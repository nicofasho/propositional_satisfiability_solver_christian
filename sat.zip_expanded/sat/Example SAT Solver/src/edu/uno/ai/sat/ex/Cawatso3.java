package edu.uno.ai.sat.ex;

import edu.uno.ai.sat.Clause;
import edu.uno.ai.sat.Literal;
import edu.uno.ai.sat.Assignment;
import edu.uno.ai.sat.Solver;
import edu.uno.ai.sat.Value;
import edu.uno.ai.sat.Variable;
import edu.uno.ai.util.ImmutableArray;
import edu.uno.ai.sat.Problem;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * SAT Solver using DPLL algorithm with caching.
 */
public class Cawatso3 extends Solver {

    private Map<String, Boolean> cache;
    private Map<String, Clause> clauseCache;
    private Map<String, Variable> variableCache;
    private Assignment solutionAssignment;

    /**
     * Constructs a new SAT solver.
     */
    public Cawatso3() {
        super("cawatso3");
        this.cache = new HashMap<>();
        this.clauseCache = new HashMap<>();
        this.variableCache = new HashMap<>();
    }

    @Override
    public boolean solve(Assignment assignment) {

        // Base case: if all clauses are satisfied
        if (assignment.countFalseClauses() == 0 && assignment.countUnknownClauses() == 0) {
            return true;
        }

        // Base case: if any clause is false
        if (assignment.countFalseClauses() > 0) {
            return false;
        }

        // Try to find a pure symbol
        Result result = findPureSymbol(assignment);
        if (result != null) {
            if (tryValue(assignment, result.P, result.value)) {
                return true;
            }
            removeSymbol(result.P);
            boolean res = solve(assignment);
            return res;
        }

        // Try to find a unit clause
        result = findUnitClause(assignment);
        if (result != null) {
            if (tryValue(assignment, result.P, result.value)) {
                return true;
            }
            removeSymbol(result.P);
            boolean res = solve(assignment);
            cache.put(assignment.toString(), res);
            return res;
        }

        // Splitting step: choose the most promising unassigned variable
        Variable P = selectMostPromisingVariable(assignment);
        if (P == null) {
            cache.put(assignment.toString(), false);
            return false; // No unassigned variables left, should not happen
        }

        boolean resTrue = tryValue(assignment, P, Value.TRUE);

        // If assigning TRUE leads to a solution, propagate it up
        if (resTrue) {
            cache.put(assignment.toString(), true);
            return true;
        }

        boolean resFalse = tryValue(assignment, P, Value.FALSE);

        // Cache and return the result
        boolean finalResult = resFalse;
        cache.put(assignment.toString(), finalResult);
        return finalResult;

    }

    private boolean tryValue(Assignment a, Variable var, Value val) {
        Value backup = a.getValue(var);
        a.setValue(var, val);
        if (solve(a)) {
            return true;
        } else {
            a.setValue(var, backup);
            return false;
        }
    }

    public void removeSymbol(Variable symbolToRemove) {
        variableCache.put(symbolToRemove.toString(), symbolToRemove);
    }

    private Variable selectMostPromisingVariable(Assignment assignment
            ) {
        // Implement a heuristic to select the most promising variable
        // For simplicity, we'll use the Most Occurrences in Clauses (MOM) heuristic
        Variable bestVariable = null;
        int maxOccurrences = -1;

        for (Variable symbol : assignment.problem.variables) {
            if (assignment.getValue(symbol) == Value.UNKNOWN && !variableCache.containsKey(symbol.toString())) {
                int occurrences = countOccurrences(symbol, assignment.problem.clauses);
                if (occurrences > maxOccurrences) {
                    maxOccurrences = occurrences;
                    bestVariable = symbol;
                }
            }
        }

        return bestVariable;
    }

    private int countOccurrences(Variable symbol, ImmutableArray<Clause> clauses) {
        int count = 0;
        for (Clause clause : clauses) {
            for (Literal literal : clause.literals) {
                if (literal.variable.equals(symbol)) {
                    count++;
                }
            }
        }
        return count;
    }

    private Variable selectUnassignedVariable(ImmutableArray<Variable> symbols, Assignment assignment) {
        for (Variable P : symbols) {
            if (assignment.getValue(P) == Value.UNKNOWN) {
                return P;
            }
        }
        return null;
    }

    private Result findPureSymbol(Assignment assignment) {
        Map<Variable, Boolean> pureSymbols = new HashMap<>();

        for (Clause clause : assignment.problem.clauses) {
            if (assignment.getValue(clause) == Value.TRUE) {
                continue;
            }

            for (Literal literal : clause.literals) {
                Boolean currentSign = pureSymbols.get(literal.variable);
                if (currentSign == null) {
                    currentSign = literal.valence;
                } else if (currentSign != literal.valence) {
                    pureSymbols.remove(literal.variable);
                } else if (currentSign == literal.valence) {
                    pureSymbols.put(literal.variable, literal.valence);
                }
            }

            clauseCache.put(clause.toString(), clause);
        }

        for (Map.Entry<Variable, Boolean> entry : pureSymbols.entrySet()) {
            if (!variableCache.containsKey(entry.getKey().toString())) {
                variableCache.put(entry.getKey().toString(), entry.getKey());
                return new Result(entry.getValue() ? Value.TRUE : Value.FALSE, entry.getKey());
            }
        }

        return null;
    }

    public Result findUnitClause(Assignment assignment) {
        for (Clause clause : assignment.problem.clauses) {
            if (clause.literals.size() == 1) {
                if (!clauseCache.containsKey(clause.toString())) {
                    clauseCache.put(clause.toString(), clause);
                    return new Result(clause.literals.get(0).valence ? Value.TRUE : Value.FALSE,
                            clause.literals.get(0).variable);
                }
            }
        }
        return null;
    }

    class Result {
        public Value value;
        public Variable P;

        public Result(Value value, Variable P) {
            this.value = value;
            this.P = P;
        }
    }
}

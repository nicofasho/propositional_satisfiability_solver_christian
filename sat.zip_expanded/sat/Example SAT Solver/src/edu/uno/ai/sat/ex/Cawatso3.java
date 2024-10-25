package edu.uno.ai.sat.ex;

import edu.uno.ai.sat.Clause;
import edu.uno.ai.sat.Literal;
import edu.uno.ai.sat.Assignment;
import edu.uno.ai.sat.Solver;
import edu.uno.ai.sat.Value;
import edu.uno.ai.sat.Variable;
import edu.uno.ai.util.ImmutableArray;
import java.util.HashMap;
import java.util.Map;

/**
 * SAT Solver using DPLL algorithm with caching.
 */
public class Cawatso3 extends Solver {

    private Map<String, Boolean> cache = new HashMap<>();

    /**
     * Constructs a new SAT solver.
     */
    public Cawatso3() {
        super("cawatso3");
    }

    @Override
    public boolean solve(Assignment assignment) {
        try {
            return DPLL_Satisfiable(assignment);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean DPLL_Satisfiable(Assignment assignment) {
        return DPLL(assignment.problem.clauses, assignment.problem.variables, assignment);
    }

    public boolean DPLL(ImmutableArray<Clause> clauses, ImmutableArray<Variable> symbols, Assignment assignment) {
        // Convert the current assignment to a string to use as a cache key
        String cacheKey = assignment.toString();

        // Check if the result is already in the cache
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        // Base case: if all clauses are satisfied
        if (assignment.countFalseClauses() == 0 && assignment.countUnknownClauses() == 0) {
            cache.put(cacheKey, true);
            return true;
        }

        // Base case: if any clause is false
        if (assignment.countFalseClauses() > 0) {
            cache.put(cacheKey, false);
            return false;
        }

        // Try to find a pure symbol
        Result result = findPureSymbol(symbols, clauses, assignment);
        if (result != null) {
            assignment.setValue(result.P, result.value);
            boolean res = DPLL(clauses, symbols, assignment);
            cache.put(cacheKey, res);
            return res;
        }

        // Try to find a unit clause
        result = findUnitClause(clauses, assignment);
        if (result != null) {
            assignment.setValue(result.P, result.value);
            boolean res = DPLL(clauses, symbols, assignment);
            cache.put(cacheKey, res);
            return res;
        }

        // Splitting step: choose the most promising unassigned variable
        Variable P = selectMostPromisingVariable(symbols, assignment, clauses);
        if (P == null) {
            cache.put(cacheKey, false);
            return false; // No unassigned variables left, should not happen
        }

        // Try assigning TRUE to the chosen variable
        Assignment assignmentCopy = assignment.clone();
        assignment.setValue(P, Value.TRUE);
        if (DPLL(clauses, symbols, assignment)) {
            cache.put(cacheKey, true);
            return true;
        }

        // Try assigning FALSE to the chosen variable
        assignmentCopy.setValue(P, Value.FALSE);
        boolean res = DPLL(clauses, symbols, assignmentCopy);
        cache.put(cacheKey, res);
        return res;
    }

    private Variable selectMostPromisingVariable(ImmutableArray<Variable> symbols, Assignment assignment,
            ImmutableArray<Clause> clauses) {
        // Implement a heuristic to select the most promising variable
        // For simplicity, we'll use the Most Occurrences in Clauses (MOM) heuristic
        Variable bestVariable = null;
        int maxOccurrences = -1;

        for (Variable symbol : symbols) {
            if (assignment.getValue(symbol) == Value.UNKNOWN) {
                int occurrences = countOccurrences(symbol, clauses);
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

    private Result findPureSymbol(ImmutableArray<Variable> symbols, ImmutableArray<Clause> clauses, Assignment assignment) {
        Map<Variable, Boolean> pureSymbols = new HashMap<>();

        for (Clause clause : clauses) {
            if (assignment.getValue(clause) == Value.TRUE) {
                continue;
            }

            for (Literal literal : clause.literals) {
                Value value = assignment.getValue(literal.variable);
                if (value == Value.UNKNOWN) {
                    Boolean currentSign = pureSymbols.get(literal.variable);
                    if (currentSign == null) {
                        pureSymbols.put(literal.variable, assignment.getValue(clause) == Value.TRUE);
                    } else if (currentSign != (assignment.getValue(clause) == Value.TRUE)) {
                        pureSymbols.remove(literal.variable);
                    }
                }
            }
        }

        for (Map.Entry<Variable, Boolean> entry : pureSymbols.entrySet()) {
            return new Result(entry.getValue() ? Value.TRUE : Value.FALSE, entry.getKey());
        }

        return null;
    }

    public Result findUnitClause(ImmutableArray<Clause> clauses, Assignment assignment) {
        for (Clause clause : clauses) {
            int unknownCount = 0;
            Variable P = null;
            for (Literal literal : clause.literals) {
                if (assignment.getValue(literal.variable) == Value.UNKNOWN) {
                    unknownCount++;
                    P = literal.variable;
                }
            }
            if (unknownCount == 1) {
                return new Result(isClauseSatisfied(P, clause, assignment), P);
            }
        }
        return null;
    }

    public Value isClauseSatisfied(Variable P, Clause clause, Assignment assignment) {
        Assignment assignmentTrue = assignment.clone();
        Assignment assignmentFalse = assignment.clone();

        assignmentTrue.setValue(P, Value.TRUE);
        assignmentFalse.setValue(P, Value.FALSE);

        if (assignmentTrue.getValue(clause) == Value.TRUE) {
            return Value.TRUE;
        }

        if (assignmentFalse.getValue(clause) == Value.TRUE) {
            return Value.FALSE;
        }

        return Value.UNKNOWN;
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

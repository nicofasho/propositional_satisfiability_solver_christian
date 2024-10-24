package edu.uno.ai.sat.ex;

import edu.uno.ai.sat.Clause;
import edu.uno.ai.sat.Literal;

import edu.uno.ai.sat.Assignment;
import edu.uno.ai.sat.Solver;
import edu.uno.ai.sat.Value;
import edu.uno.ai.sat.Variable;
import edu.uno.ai.util.ImmutableArray;

/**
 * 
 * @author Your Name
 */
public class Cawatso3 extends Solver {

    /**
     * Constructs a new random SAT solver. You should change the string below
     * from "random" to your ID. You should also change the name of this class.
     * In Eclipse, you can do that easily by right-clicking on this file
     * (RandomAgent.java) in the Package Explorer and choosing Refactor > Rename.
     */
    public Cawatso3() {
        super("cawatso3");
    }

    @Override
    public boolean solve(Assignment assignment) {
        // If the problem has no variables, it is trivially true or false.
        // try {
        // if (assignment.problem.variables.size() == 0)
        // return assignment.getValue() == Value.TRUE;
        // else {
        // // Keep trying until the assignment is satisfying.
        // while (assignment.getValue() != Value.TRUE) {
        // // Choose a variable whose value will be set.
        // Variable variable = chooseVariable(assignment);
        // // Choose 'true' or 'false' at random.
        // Value value;
        // if (random.nextBoolean())
        // value = Value.TRUE;
        // else
        // value = Value.FALSE;
        // // Assign the chosen value to the chosen variable.
        // assignment.setValue(variable, value);
        // }
        // // Return success. (Note, if the problem cannot be solved, this
        // // solver will run until it reaches the operations or time limit.)
        // return true;
        // }
        // } catch (Exception e) {
        // return false;
        // }
        try {
            return DPLL_Satisfiable(assignment);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean DPLL_Satisfiable(Assignment assignment) {
        return DPLL(assignment.problem.clauses, assignment.problem.variables, assignment);
    }

    public boolean DPLL(ImmutableArray<Clause> clauses, ImmutableArray<Variable> symbols, Assignment assignment) {
        if (assignment.countFalseClauses() == 0) {
            return true;
        }

        if (assignment.countFalseClauses() > 0) {
            return false;
        }

        Result result = findPureSymbol(symbols, clauses, assignment);

        if (result != null) {
            assignment.setValue(result.P, result.value);
            return DPLL(clauses, symbols, assignment);
        }

        result = findUnitClause(clauses, assignment);

        if (result != null) {
            assignment.setValue(result.P, result.value);
            return DPLL(clauses, symbols, assignment);
        }

        Variable P = symbols.get(0);
        ImmutableArray<Variable> rest = createSubArray(symbols);

        Assignment assignmentA = assignment.clone();
        assignmentA.setValue(P, Value.TRUE);

        Assignment assignmentB = assignment.clone();
        assignmentB.setValue(P, Value.FALSE);

        return DPLL(clauses, rest, assignmentA) || DPLL(clauses, rest, assignmentB);

    }

    public ImmutableArray<Variable> createSubArray(ImmutableArray<Variable> symbols) {
        Variable[] list = new Variable[symbols.size() - 1];
        for (int i = 1; i < symbols.size(); i++) {
            list[i - 1] = symbols.get(i);
        }
        return new ImmutableArray<Variable>(list);
    }

    public Result findPureSymbol(ImmutableArray<Variable> symbols, ImmutableArray<Clause> clauses, Assignment assignment) {
        for (Variable P : symbols) {
            Boolean value = null;
            boolean found = false;
            for (Clause clause : clauses) {
                for (Literal literal : clause.literals) {
                    if (literal.variable.equals(P)) {
                        if (value == null) {
                            value = literal.valence;
                        } else if (value != literal.valence) {
                            found = true;
                            break;
                        }
                    }
                }
                if (found)
                    break;
            }

            if (!found && value != null) {
                return new Result(value ? Value.TRUE : Value.FALSE, P);
            }
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
                return new Result(isClauseSatisfied(clause) ? Value.TRUE : Value.FALSE, P);
            }
        }
        return null;
    }

    public boolean isClauseSatisfied(Clause clause) {
        for (Literal literal : clause.literals) {
            if (literal.valence == true) {
                return true;
            }
        }
        return false;
    }

    class Result {
        public Value value;
        public Variable P;

        public Result(Value value, Variable P) {
            this.value = value;
            this.P = P;
        }
    }

    /**
     * Randomly choose a variable from the problem whose value will be set. If
     * any variables have the value 'unknown,' choose one of those first;
     * otherwise choose any variable.
     * 
     * @param assignment the assignment being worked on
     * @return a variable, chosen randomly
     */
    // private final Variable chooseVariable(Assignment assignment) {
    // // This list will hold all variables whose current value is 'unknown.'
    // ArrayList<Variable> unknown = new ArrayList<>();
    // // Loop through all the variables in the problem and find ones whose
    // // current value is 'unknown.'
    // for (Variable variable : assignment.problem.variables)
    // if (assignment.getValue(variable) == Value.UNKNOWN)
    // unknown.add(variable);
    // // If any variables are 'unknown,' choose one of them randomly.
    // if (unknown.size() > 0)
    // return unknown.get(random.nextInt(unknown.size()));
    // // Otherwise, choose any variable from the problem at random.
    // else
    // return
    // assignment.problem.variables.get(random.nextInt(assignment.problem.variables.size()));
    // }
}

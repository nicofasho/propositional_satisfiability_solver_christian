package edu.uno.ai.sat.ex;

import edu.uno.ai.sat.Assignment;
import edu.uno.ai.sat.Clause;
import edu.uno.ai.sat.Literal;
import edu.uno.ai.sat.Solver;
import edu.uno.ai.sat.Value;
import edu.uno.ai.sat.Variable;
import java.util.Iterator;

public class Cawatso3 extends Solver {
   public Cawatso3() {
      super("Cawatso3");
   }

   /**
    * Attempts to solve the SAT problem with the given assignment.
    * @param assignment The current assignment of variables.
    * @return True if the problem is satisfiable, false otherwise.
    */
   public boolean solve(Assignment assignment) {
      // Check if the current assignment is already a solution
      if (assignment.getValue() == Value.TRUE) {
         return true;
      } else if (assignment.getValue() == Value.FALSE) {
         return false;
      } else {
         // Perform unit propagation
         Literal unitLiteral = findUnitLiteral(assignment);
         if (unitLiteral != null) {
            return assignAndSolve(assignment, unitLiteral.variable, unitLiteral.valence ? Value.TRUE : Value.FALSE);
         }

         // Perform pure literal elimination
         Literal pureLiteral = findPureLiteral(assignment);
         if (pureLiteral != null) {
            return assignAndSolve(assignment, pureLiteral.variable, pureLiteral.valence ? Value.TRUE : Value.FALSE);
         }

         // Select an unassigned variable and try both true and false assignments
         Variable unassignedVariable = selectUnassignedVariable(assignment);
         if (unassignedVariable != null) {
            return assignAndSolve(assignment, unassignedVariable, Value.TRUE) || assignAndSolve(assignment, unassignedVariable, Value.FALSE);
         }

         // If no unassigned variables are left, the problem is unsatisfiable
         return false;
      }
   }

   /**
    * Finds a unit literal in the current assignment.
    * @param assignment The current assignment of variables.
    * @return A unit literal, or null if no unit literals are found.
    */
   private Literal findUnitLiteral(Assignment assignment) {
      for (Clause clause : assignment.problem.clauses) {
         if (assignment.getValue(clause) == Value.UNKNOWN && assignment.countUnknownLiterals(clause) == 1) {
            for (Literal literal : clause.literals) {
               if (assignment.getValue(literal) == Value.UNKNOWN) {
                  return literal;
               }
            }
         }
      }
      return null;
   }

   /**
    * Finds a pure literal in the current assignment.
    * @param assignment The current assignment of variables.
    * @return A pure literal, or null if no pure literals are found.
    */
   private Literal findPureLiteral(Assignment assignment) {
      for (Variable variable : assignment.problem.variables) {
         if (assignment.getValue(variable) == Value.UNKNOWN) {
            Literal positiveLiteral = null;
            Literal negativeLiteral = null;
            for (Literal literal : variable.literals) {
               if (assignment.getValue(literal.clause) == Value.UNKNOWN) {
                  if (literal.valence) {
                     if (positiveLiteral == null) {
                        positiveLiteral = literal;
                     }
                  } else {
                     if (negativeLiteral == null) {
                        negativeLiteral = literal;
                     }
                  }
               }
            }
            if (positiveLiteral != null && negativeLiteral == null) {
               return positiveLiteral;
            } else if (positiveLiteral == null && negativeLiteral != null) {
               return negativeLiteral;
            }
         }
      }
      return null;
   }

   /**
    * Selects an unassigned variable in the current assignment.
    * @param assignment The current assignment of variables.
    * @return An unassigned variable, or null if all variables are assigned.
    */
   private Variable selectUnassignedVariable(Assignment assignment) {
      for (Variable variable : assignment.problem.variables) {
         if (assignment.getValue(variable) == Value.UNKNOWN) {
            return variable;
         }
      }
      return null;
   }

   /**
    * Assigns a value to a variable and attempts to solve the problem recursively.
    * @param assignment The current assignment of variables.
    * @param variable The variable to assign a value to.
    * @param value The value to assign to the variable.
    * @return True if the problem is satisfiable with the new assignment, false otherwise.
    */
   private boolean assignAndSolve(Assignment assignment, Variable variable, Value value) {
      Value originalValue = assignment.getValue(variable);
      assignment.setValue(variable, value);
      if (solve(assignment)) {
         return true;
      } else {
         assignment.setValue(variable, originalValue);
         return false;
      }
   }
}

package edu.uno.ai.sat.ex;

import edu.uno.ai.sat.Assignment;
import edu.uno.ai.sat.Clause;
import edu.uno.ai.sat.Literal;
import edu.uno.ai.sat.Solver;
import edu.uno.ai.sat.Value;
import edu.uno.ai.sat.Variable;
import java.util.Iterator;

public class Cawatso4 extends Solver {
   public Cawatso4() {
      super("Cawatso4");
   }

   public boolean solve(Assignment assignment) {
      if (assignment.getValue() == Value.TRUE) {
         return true;
      } else if (assignment.getValue() == Value.FALSE) {
         return false;
      } else {
         Assignment currentAssignment = assignment;
         Iterator clauseIterator = assignment.problem.clauses.iterator();

         Literal unitLiteral;
         label104:
         while (true) {
            if (!clauseIterator.hasNext()) {
               unitLiteral = null;
               break;
            }

            Clause clause = (Clause) clauseIterator.next();
            if (currentAssignment.getValue(clause) == Value.UNKNOWN && currentAssignment.countUnknownLiterals(clause) == 1) {
               for (Literal literal : clause.literals) {
                  if (currentAssignment.getValue(literal) == Value.UNKNOWN) {
                     unitLiteral = literal;
                     break label104;
                  }
               }
            }
         }

         if (unitLiteral != null) {
            return unitLiteral.valence ? this.assignValue(assignment, unitLiteral.variable, Value.TRUE) : this.assignValue(assignment, unitLiteral.variable, Value.FALSE);
         } else {
            currentAssignment = assignment;
            clauseIterator = assignment.problem.variables.iterator();

            while (true) {
               if (!clauseIterator.hasNext()) {
                  unitLiteral = null;
                  break;
               }

               Variable variable = (Variable) clauseIterator.next();
               if (currentAssignment.getValue(variable) == Value.UNKNOWN) {
                  Literal positiveLiteral = null;
                  Literal negativeLiteral = null;

                  for (Literal literal : variable.literals) {
                     if (currentAssignment.getValue(literal.clause) == Value.UNKNOWN) {
                        if (literal.valence && positiveLiteral == null) {
                           positiveLiteral = literal;
                        } else if (!literal.valence && negativeLiteral == null) {
                           negativeLiteral = literal;
                        }
                     }
                  }

                  if (positiveLiteral != null && negativeLiteral == null || positiveLiteral == null && negativeLiteral != null) {
                     unitLiteral = positiveLiteral == null ? negativeLiteral : positiveLiteral;
                     break;
                  }
               }
            }

            if (unitLiteral != null) {
               return unitLiteral.valence ? this.assignValue(assignment, unitLiteral.variable, Value.TRUE) : this.assignValue(assignment, unitLiteral.variable, Value.FALSE);
            } else {
               Variable unassignedVariable = findUnassignedVariable(assignment);
               return this.assignValue(assignment, unassignedVariable, Value.TRUE) || this.assignValue(assignment, unassignedVariable, Value.FALSE);
            }
         }
      }
   }

   protected final boolean assignValue(Assignment assignment, Variable variable, Value value) {
      Value originalValue = assignment.getValue(variable);
      assignment.setValue(variable, value);
      if (this.solve(assignment)) {
         return true;
      } else {
         assignment.setValue(variable, originalValue);
         return false;
      }
   }

   protected static Variable findUnassignedVariable(Assignment assignment) {
      for (Variable variable : assignment.problem.variables) {
         if (assignment.getValue(variable) == Value.UNKNOWN) {
            return variable;
         }
      }

      return null;
   }
}
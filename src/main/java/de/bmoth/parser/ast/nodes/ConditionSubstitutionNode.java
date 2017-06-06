package de.bmoth.parser.ast.nodes;

public class ConditionSubstitutionNode extends SubstitutionNode {
    private PredicateNode condition;
    private SubstitutionNode substitution;

    public enum ConditionSubstitutionKind {
        PRECONDITION, ASSERT
    }

    public ConditionSubstitutionNode(ConditionSubstitutionKind kind, PredicateNode condition,
            SubstitutionNode substitution) {
        this.condition = condition;
        this.substitution = substitution;
        super.setAssignedVariables(substitution.getAssignedVariables());
    }

    public SubstitutionNode getSubstitution() {
        return substitution;
    }

    public PredicateNode getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        return "SELECT " + condition + " THEN " + substitution + " END";
    }

    public void setSubstitution(SubstitutionNode substitution) {
        this.substitution = substitution;
    }

    public void setCondition(PredicateNode predicate) {
        this.condition = predicate;
    }
}
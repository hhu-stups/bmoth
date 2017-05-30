package de.bmoth.parser.ast;

import java.util.List;
import java.util.stream.Collectors;

import de.bmoth.parser.ast.nodes.AnySubstitutionNode;
import de.bmoth.parser.ast.nodes.CastPredicateExpressionNode;
import de.bmoth.parser.ast.nodes.ExprNode;
import de.bmoth.parser.ast.nodes.ExpressionOperatorNode;
import de.bmoth.parser.ast.nodes.IdentifierExprNode;
import de.bmoth.parser.ast.nodes.IdentifierPredicateNode;
import de.bmoth.parser.ast.nodes.Node;
import de.bmoth.parser.ast.nodes.NumberNode;
import de.bmoth.parser.ast.nodes.ParallelSubstitutionNode;
import de.bmoth.parser.ast.nodes.PredicateNode;
import de.bmoth.parser.ast.nodes.PredicateOperatorNode;
import de.bmoth.parser.ast.nodes.PredicateOperatorWithExprArgsNode;
import de.bmoth.parser.ast.nodes.QuantifiedExpressionNode;
import de.bmoth.parser.ast.nodes.QuantifiedPredicateNode;
import de.bmoth.parser.ast.nodes.SelectSubstitutionNode;
import de.bmoth.parser.ast.nodes.SingleAssignSubstitutionNode;
import de.bmoth.parser.ast.nodes.SubstitutionNode;

public class AbstractASTTransformation implements AbstractVisitor<Node, Void> {

    boolean changed = false;

    public void setChanged() {
        this.changed = true;
    }

    public boolean hasChanged() {
        return changed;
    }

    public void resetChanged() {
        this.changed = false;
    }

    @Override
    public Node visitPredicateOperatorNode(PredicateOperatorNode node, Void expected) {
        List<PredicateNode> list = node.getPredicateArguments().stream()
                .map(predNode -> (PredicateNode) visitPredicateNode(predNode, expected)).collect(Collectors.toList());
        node.setPredicateList(list);
        return node;
    }

    @Override
    public Node visitPredicateOperatorWithExprArgs(PredicateOperatorWithExprArgsNode node, Void expected) {
        final List<ExprNode> argumentList = node.getExpressionNodes().stream()
                .map(exprNode -> (ExprNode) visitExprNode(exprNode, expected)).collect(Collectors.toList());
        node.setArgumentsList(argumentList);
        return node;
    }

    @Override
    public Node visitExprOperatorNode(ExpressionOperatorNode node, Void expected) {
        final List<ExprNode> arguments = node.getExpressionNodes().stream()
                .map(exprNode -> (ExprNode) visitExprNode(exprNode, expected)).collect(Collectors.toList());
        node.setExpressionList(arguments);
        return node;
    }

    @Override
    public Node visitIdentifierExprNode(IdentifierExprNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitCastPredicateExpressionNode(CastPredicateExpressionNode node, Void expected) {
        Node arg = visitPredicateNode(node.getPredicate(), expected);
        node.setArg((PredicateNode) arg);
        return node;
    }

    @Override
    public Node visitNumberNode(NumberNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitSelectSubstitutionNode(SelectSubstitutionNode node, Void expected) {
        node.setCondition((PredicateNode) visitPredicateNode(node.getCondition(), expected));
        node.setSubstitution((SubstitutionNode) visitSubstitutionNode(node.getSubstitution(), expected));
        return node;
    }

    @Override
    public Node visitSingleAssignSubstitution(SingleAssignSubstitutionNode node, Void expected) {
        node.setValue((ExprNode) visitExprNode(node.getValue(), expected));
        return node;
    }

    @Override
    public Node visitAnySubstitution(AnySubstitutionNode node, Void expected) {
        node.setPredicate((PredicateNode) visitPredicateNode(node.getWherePredicate(), expected));
        node.setSubstitution((SubstitutionNode) visitSubstitutionNode(node.getThenSubstitution(), expected));
        return node;
    }

    @Override
    public Node visitParallelSubstitutionNode(ParallelSubstitutionNode node, Void expected) {
        List<SubstitutionNode> substitutions = node.getSubstitutions().stream()
                .map(sub -> (SubstitutionNode) visitSubstitutionNode(node, expected)).collect(Collectors.toList());
        node.setSubstitutions(substitutions);
        return node;
    }

    @Override
    public Node visitIdentifierPredicateNode(IdentifierPredicateNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitQuantifiedExpressionNode(QuantifiedExpressionNode node, Void expected) {
        PredicateNode visitPredicateNode = (PredicateNode) visitPredicateNode(node.getPredicateNode(), expected);
        node.setPredicate(visitPredicateNode);
        if (node.getExpressionNode() != null) {
            ExprNode expr = (ExprNode) visitExprNode(node.getExpressionNode(), expected);
            node.setExpr(expr);
        }
        return node;
    }

    @Override
    public Node visitQuantifiedPredicateNode(QuantifiedPredicateNode node, Void expected) {
        PredicateNode pred = (PredicateNode) visitPredicateNode(node.getPredicateNode(), expected);
        node.setPredicate(pred);
        return node;
    }

}

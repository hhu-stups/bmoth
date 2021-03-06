package de.bmoth.backend.ltl.transformation;

import de.bmoth.parser.ast.nodes.Node;
import de.bmoth.parser.ast.nodes.ltl.LTLInfixOperatorNode;
import de.bmoth.parser.ast.visitors.ASTTransformation;

import static de.bmoth.backend.ltl.LTLTransformationUtil.*;
import static de.bmoth.parser.ast.nodes.ltl.LTLInfixOperatorNode.Kind.AND;
import static de.bmoth.parser.ast.nodes.ltl.LTLKeywordNode.Kind.TRUE;

public class RemoveTrueFromAnd implements ASTTransformation {

    @Override
    public boolean canHandleNode(Node node) {
        return isOperator(node, AND) && (containsLeft(node, TRUE) || containsRight(node, TRUE));
    }

    @Override
    public Node transformNode(Node node) {
        LTLInfixOperatorNode andNode = (LTLInfixOperatorNode) node;

        if (isOperator(andNode.getLeft(), TRUE)) {
            return andNode.getRight();
        } else {
            return andNode.getLeft();
        }
    }
}

package de.bmoth.parser.ast.nodes.ltl;

import de.bmoth.parser.ast.nodes.PredicateNode;

import java.util.*;

public class BuechiAutomaton {

    private int nodeCounter = 0;
    private List<LTLInfixOperatorNode> subFormulasForAcceptance = new ArrayList<>();
    private List<List<BuechiAutomatonNode>> acceptingStateSets = new ArrayList<>();
    private Set<BuechiAutomatonNode> initialStates = new LinkedHashSet<>();

    private final Set<BuechiAutomatonNode> finalNodeSet;

    public BuechiAutomaton() {
        LTLKeywordNode ltlKeywordNode = new LTLKeywordNode(LTLKeywordNode.Kind.TRUE);
        LTLPrefixOperatorNode ltlNode = new LTLPrefixOperatorNode(LTLPrefixOperatorNode.Kind.GLOBALLY, ltlKeywordNode);

        this.finalNodeSet = createGraph(ltlNode);
        labelNodes();
        determineInitialsAndSuccessors();
    }

    public BuechiAutomaton(LTLNode ltlNode) {
        this.finalNodeSet = createGraph(ltlNode);
        labelNodes();
        determineInitialsAndSuccessors();
    }

    private String newName() {
        nodeCounter++;
        return "node" + nodeCounter;
    }

    private Boolean checkForContradiction(LTLNode ltlNode, Set<LTLNode> processedNodes) {
        PredicateNode negatedNode = ((LTLBPredicateNode) ltlNode).getPredicate().getNegatedPredicateNode();
        for (LTLNode processedNode : processedNodes) {
            if (processedNode instanceof LTLBPredicateNode
                && ((LTLBPredicateNode) processedNode).getPredicate().equalAst(negatedNode)) {
                return true;
            }
        }
        return false;
    }

    private Boolean ltlNodeIsInList(LTLNode ltlNode, Set<LTLNode> processed) {
        for (LTLNode processedNode : processed) {
            if (ltlNode.equalAst(processedNode)) {
                return true;
            }
        }
        return false;
    }

    private Boolean compareLTLNodeSets(Set<LTLNode> nodeSet, Set<LTLNode> processedNodeSet) {
        if (nodeSet.size() == processedNodeSet.size()) {
            Set<LTLNode> nodeProcessed = new LinkedHashSet<>(nodeSet);
            Set<LTLNode> nodeInSetProcessed = new LinkedHashSet<>(processedNodeSet);
            Iterator<LTLNode> nodeIterator = nodeProcessed.iterator();
            while (nodeIterator.hasNext()) {
                LTLNode ltlNode = nodeIterator.next();
                Iterator<LTLNode> nodeInSetIterator = nodeInSetProcessed.iterator();
                while (nodeInSetIterator.hasNext()) {
                    LTLNode ltlNodeInSet = nodeInSetIterator.next();
                    if (ltlNode.equalAst(ltlNodeInSet)) {
                        nodeIterator.remove();
                        nodeInSetIterator.remove();
                        break;
                    }
                }
            }
            return (nodeProcessed.isEmpty() && nodeInSetProcessed.isEmpty());
        } else {
            return false;
        }
    }

    private BuechiAutomatonNode buechiNodeIsInNodeSet(BuechiAutomatonNode buechiNode, Set<BuechiAutomatonNode> nodesSet) {
        // Check whether the finished node is already in the list (determined by the same Old- and Next-sets).
        for (BuechiAutomatonNode nodeInSet : nodesSet) {
            boolean processedEquals = compareLTLNodeSets(buechiNode.processed, nodeInSet.processed);
            boolean nextEquals = compareLTLNodeSets(buechiNode.next, nodeInSet.next);
            if (processedEquals && nextEquals) {
                return nodeInSet;
            }
        }
        return null;
    }

    private Set<LTLNode> new1(LTLInfixOperatorNode ltlNode) {
        Set<LTLNode> newNodes = new LinkedHashSet<>();
        if (ltlNode.getKind() == LTLInfixOperatorNode.Kind.RELEASE) {
            newNodes.add(ltlNode.getRight());
        } else {
            // Until, or
            newNodes.add(ltlNode.getLeft());
        }
        return newNodes;
    }

    private Set<LTLNode> new2(LTLInfixOperatorNode ltlNode) {
        Set<LTLNode> newNodes = new LinkedHashSet<>();
        newNodes.add(ltlNode.getRight());
        if (ltlNode.getKind() == LTLInfixOperatorNode.Kind.RELEASE) {
            newNodes.add(ltlNode.getLeft());
        }
        return newNodes;
    }

    private Set<LTLNode> next1(LTLInfixOperatorNode ltlNode) {
        Set<LTLNode> newNodes = new LinkedHashSet<>();
        if (ltlNode.getKind() == LTLInfixOperatorNode.Kind.UNTIL || ltlNode.getKind() == LTLInfixOperatorNode.Kind.RELEASE) {
            newNodes.add(ltlNode);
        }
        // In case of or an empty list is returned
        return newNodes;
    }

    private BuechiAutomatonNode buildFirstNodeInSplit(BuechiAutomatonNode buechiNode, LTLNode subNode, Set<LTLNode> newProcessed) {
        // Prepare the different parts of the first new node created for Until, Release and Or
        Set<LTLNode> unprocessed = new1((LTLInfixOperatorNode) subNode);
        unprocessed.removeAll(buechiNode.processed);
        unprocessed.addAll(buechiNode.unprocessed);
        Set<LTLNode> next = new LinkedHashSet<>(buechiNode.next);
        next.addAll(next1((LTLInfixOperatorNode) subNode));

        return new BuechiAutomatonNode(newName(), new LinkedHashSet<>(buechiNode.incoming),
            unprocessed, newProcessed, next);
    }

    private BuechiAutomatonNode buildSecondNodeInSplit(BuechiAutomatonNode buechiNode, LTLNode subNode, Set<LTLNode> processed) {
        // Prepare the different parts of the second new node created for Until, Release and Or
        Set<LTLNode> unprocessed = new2((LTLInfixOperatorNode) subNode);
        unprocessed.removeAll(buechiNode.processed);
        unprocessed.addAll(buechiNode.unprocessed);

        return new BuechiAutomatonNode(newName(), new LinkedHashSet<>(buechiNode.incoming),
            unprocessed, processed, new LinkedHashSet<>(buechiNode.next));
    }

    private Set<BuechiAutomatonNode> handleProcessedNode(BuechiAutomatonNode buechiNode, Set<BuechiAutomatonNode> nodeSet) {
        // Add a processed node to the nodeSet or update it.
        BuechiAutomatonNode nodeInSet = buechiNodeIsInNodeSet(buechiNode, nodeSet);
        if (nodeInSet != null) {
            nodeInSet.incoming.addAll(buechiNode.incoming);
            return nodeSet;
        } else {
            Set<BuechiAutomatonNode> incoming = new LinkedHashSet<>();
            incoming.add(buechiNode);
            nodeSet.add(buechiNode);
            return expand(new BuechiAutomatonNode(newName(), incoming, new LinkedHashSet<>(buechiNode.next),
                new LinkedHashSet<>(), new LinkedHashSet<>()), nodeSet);
        }
    }

    private Set<BuechiAutomatonNode> handleInfixOperatorNode(BuechiAutomatonNode buechiNode, LTLNode ltlNode,
                                                             Set<BuechiAutomatonNode> nodeSet) {
        if (((LTLInfixOperatorNode) ltlNode).getKind() == LTLInfixOperatorNode.Kind.AND) {
            // And
            Set<LTLNode> unprocessed = new LinkedHashSet<>(buechiNode.unprocessed);
            unprocessed.add(((LTLInfixOperatorNode) ltlNode).getLeft());
            unprocessed.add(((LTLInfixOperatorNode) ltlNode).getRight());
            unprocessed.removeAll(buechiNode.processed);

            Set<LTLNode> processed = new LinkedHashSet<>(buechiNode.processed);
            processed.add(ltlNode);

            return expand(new BuechiAutomatonNode(buechiNode.name, new LinkedHashSet<>(buechiNode.incoming),
                unprocessed, processed, new LinkedHashSet<>(buechiNode.next)), nodeSet);
        } else {
            // Until, Release, Or: Split the node in two
            if ((((LTLInfixOperatorNode) ltlNode).getKind() == LTLInfixOperatorNode.Kind.UNTIL) ||
                (((LTLInfixOperatorNode) ltlNode).getKind() == LTLInfixOperatorNode.Kind.RELEASE)) {
                subFormulasForAcceptance.add((LTLInfixOperatorNode) ltlNode);
            }
            Set<LTLNode> processed = new LinkedHashSet<>(buechiNode.processed);
            processed.add(ltlNode);
            return expand(buildSecondNodeInSplit(buechiNode, ltlNode, processed),
                expand(buildFirstNodeInSplit(buechiNode, ltlNode, processed), nodeSet));
        }
    }

    private Set<BuechiAutomatonNode> handlePrefixOperatorNode(BuechiAutomatonNode buechiNode, LTLNode ltlNode,
                                                              Set<BuechiAutomatonNode> nodeSet) {
        if (((LTLPrefixOperatorNode) ltlNode).getKind() == LTLPrefixOperatorNode.Kind.NEXT) {
            // Next
            Set<LTLNode> processed = new LinkedHashSet<>(buechiNode.processed);
            processed.add(ltlNode);
            Set<LTLNode> next = new LinkedHashSet<>(buechiNode.next);
            next.add(((LTLPrefixOperatorNode) ltlNode).getArgument());

            return expand(new BuechiAutomatonNode(buechiNode.name + "_1", new LinkedHashSet<>(buechiNode.incoming),
                new LinkedHashSet<>(buechiNode.unprocessed), processed, next), nodeSet);
        } else {
            // Not
            buechiNode.processed.add(ltlNode);
            return expand(buechiNode, nodeSet);
        }
    }

    private Set<BuechiAutomatonNode> expand(BuechiAutomatonNode buechiNode, Set<BuechiAutomatonNode> nodeSet) {
        if (buechiNode.unprocessed.isEmpty()) {
            // The current node is completely processed and can be added to the list (or, in case he was
            // already added before, updated).
            return handleProcessedNode(buechiNode, nodeSet);
        } else {
            Iterator<LTLNode> iterator = buechiNode.unprocessed.iterator();
            LTLNode ltlNode = iterator.next();
            iterator.remove();

            if (ltlNode instanceof LTLKeywordNode) {
                // True, False
                if (((LTLKeywordNode) ltlNode).getKind() == LTLKeywordNode.Kind.FALSE) {
                    // Discard the current node
                    return nodeSet;
                } else {
                    buechiNode.processed.add(ltlNode);
                    return expand(buechiNode, nodeSet);
                }
            } else if (ltlNode instanceof LTLBPredicateNode) {
                // B predicate
                if (!checkForContradiction(ltlNode, buechiNode.processed)) {
                    buechiNode.processed.add(ltlNode);
                    return expand(buechiNode, nodeSet);
                } else {
                    return nodeSet;
                }
            } else if (ltlNode instanceof LTLPrefixOperatorNode) {
                return handlePrefixOperatorNode(buechiNode, ltlNode, nodeSet);
            } else if (ltlNode instanceof LTLInfixOperatorNode) {
                return handleInfixOperatorNode(buechiNode, ltlNode, nodeSet);
            }
        }
        return nodeSet;
    }

    private Set<BuechiAutomatonNode> createGraph(LTLNode node) {
        Set<BuechiAutomatonNode> initIncoming = new LinkedHashSet<>();
        initIncoming.add(new BuechiAutomatonNode("init", new LinkedHashSet<>(), new LinkedHashSet<>(),
            new LinkedHashSet<>(), new LinkedHashSet<>()));
        Set<LTLNode> unprocessed = new LinkedHashSet<>();
        unprocessed.add(node);
        return expand(new BuechiAutomatonNode(newName(), initIncoming, unprocessed, new LinkedHashSet<>(),
            new LinkedHashSet<>()), new LinkedHashSet<>());
    }

    private void labelNodes() {
        for (BuechiAutomatonNode buechiNode : finalNodeSet) {
            buechiNode.label();
        }
        for (LTLInfixOperatorNode untilNode : subFormulasForAcceptance) {
            List<BuechiAutomatonNode> acceptingStateSet = new ArrayList<>();
            for (BuechiAutomatonNode buechiNode : finalNodeSet) {
                if (!ltlNodeIsInList(untilNode, buechiNode.processed) ||
                    ltlNodeIsInList(untilNode.getRight(), buechiNode.processed)) {
                    buechiNode.isAcceptingState = true;
                    acceptingStateSet.add(buechiNode);
                }
            }
            acceptingStateSets.add(acceptingStateSet);
        }
    }

    private void determineInitialsAndSuccessors() {
        for (BuechiAutomatonNode node : finalNodeSet) {
            for (BuechiAutomatonNode incomingNode : node.incoming) {
                incomingNode.successors.add(node);
            }
            if (node.isInitialState) {
                initialStates.add(node);
            }
        }
    }

    public String toString() {
        StringJoiner nodesString = new StringJoiner(",\n\n", "", "");
        for (BuechiAutomatonNode node : finalNodeSet) {
            nodesString.add(node.toString());
        }
        StringJoiner acceptingString = new StringJoiner(", ", "[", "]");
        for (List<BuechiAutomatonNode> acceptingStateSet : acceptingStateSets) {
            StringJoiner acceptingStatesString = new StringJoiner(", ", "(", ")");
            for (BuechiAutomatonNode node : acceptingStateSet) {
                acceptingStatesString.add(node.name);
            }
            acceptingString.add(acceptingStatesString.toString());
        }
        nodesString.add("Accepting state sets: " + acceptingString.toString());
        return nodesString.toString();
    }

    public Set<BuechiAutomatonNode> getFinalNodeSet() {
        return finalNodeSet;
    }

    public Set<BuechiAutomatonNode> getInitialStates() {
        return initialStates;
    }
}

package de.bmoth.parser.ast;

import de.bmoth.antlr.BMoThParser.FormulaContext;
import org.antlr.v4.runtime.Token;

import java.util.LinkedHashMap;

public class FormulaAnalyser extends AbstractAnalyser {
    final FormulaContext formula;
    final LinkedHashMap<String, Token> implicitDeclarations = new LinkedHashMap<>();

    public FormulaAnalyser(FormulaContext formula) {
        this.formula = formula;

        ScopeChecker scopeChecker = new ScopeChecker(this);
        formula.accept(scopeChecker);
    }

    @Override
    public void identifierNodeFound(Token identifierToken) {
        String name = identifierToken.getText();
        if (implicitDeclarations.containsKey(name)) {
            declarationReferences.put(identifierToken, implicitDeclarations.get(name));
        } else {
            implicitDeclarations.put(name, identifierToken);
            declarationReferences.put(identifierToken, identifierToken);
        }
    }

}

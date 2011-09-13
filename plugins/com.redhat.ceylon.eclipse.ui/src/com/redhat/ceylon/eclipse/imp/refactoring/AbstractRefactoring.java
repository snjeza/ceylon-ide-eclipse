package com.redhat.ceylon.eclipse.imp.refactoring;

import static com.redhat.ceylon.eclipse.imp.parser.CeylonSourcePositionLocator.getTokenIterator;

import java.util.Iterator;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.imp.services.IASTFindReplaceTarget;
import org.eclipse.imp.services.IQuickFixInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.redhat.ceylon.compiler.typechecker.context.PhasedUnit;
import com.redhat.ceylon.compiler.typechecker.tree.Node;
import com.redhat.ceylon.compiler.typechecker.tree.Tree;
import com.redhat.ceylon.eclipse.imp.builder.CeylonBuilder;
import com.redhat.ceylon.eclipse.imp.parser.CeylonParseController;
import com.redhat.ceylon.eclipse.imp.parser.CeylonSourcePositionLocator;

public abstract class AbstractRefactoring extends Refactoring {
    
    final IProject project;
    final IFile sourceFile;
    final Node node;
    final Tree.CompilationUnit rootNode;
    final CommonTokenStream tokenStream;
   
    /*public AbstractRefactoring(IQuickFixInvocationContext context) {
        sourceFile = context.getModel().getFile();
        project = sourceFile.getProject();
        PhasedUnit pu = CeylonBuilder.getPhasedUnit(sourceFile);
        rootNode = pu.getCompilationUnit();
        tokenStream = pu.getTokenStream();
        node = CeylonSourcePositionLocator.findNode(rootNode, context.getOffset(),
                context.getOffset()+context.getLength());
    }*/
    
    public AbstractRefactoring(ITextEditor editor) {

        IASTFindReplaceTarget frt = (IASTFindReplaceTarget) editor;
        IEditorInput input = editor.getEditorInput();
        CeylonParseController parseController = (CeylonParseController) frt.getParseController();
        tokenStream = parseController.getTokenStream();
        rootNode = parseController.getRootNode();

        if (input instanceof IFileEditorInput) {
            IFileEditorInput fileInput = (IFileEditorInput) input;
            sourceFile = fileInput.getFile();
            project = fileInput.getFile().getProject();
            node = CeylonSourcePositionLocator.findNode(rootNode, frt);
        }
        else {
            sourceFile = null;
            project = null;
            node = null;
        }
        
    }
    
    String guessName() {
        Node identifyingNode = node;
        if (identifyingNode instanceof Tree.Expression) {
            identifyingNode = ((Tree.Expression) identifyingNode).getTerm();
        }
        if (identifyingNode instanceof Tree.InvocationExpression) {
            identifyingNode = ((Tree.InvocationExpression) identifyingNode).getPrimary();
        }
        if (identifyingNode instanceof Tree.StaticMemberOrTypeExpression) {
            String id = ((Tree.StaticMemberOrTypeExpression) identifyingNode).getIdentifier().getText();
            return Character.toLowerCase(id.charAt(0)) + 
                    id.substring(1);
        }
        else {
            return "temp";
        }
    }

    String toString(final Tree.Term term) {
        return toString(term, tokenStream);
    }
    
    public static String toString(final Tree.Term term, CommonTokenStream tokenStream) {
        Integer start = term.getStartIndex();
        int length = term.getStopIndex()-start+1;
        Region region = new Region(start, length);
        StringBuilder exp = new StringBuilder();
        for (Iterator<Token> ti = getTokenIterator(tokenStream, region); 
                ti.hasNext();) {
            exp.append(ti.next().getText());
        }
        return exp.toString();
    }

}
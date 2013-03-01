package com.redhat.ceylon.eclipse.code.quickfix;

import static com.redhat.ceylon.eclipse.code.outline.CeylonLabelProvider.CORRECTION;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import com.redhat.ceylon.compiler.typechecker.model.Declaration;
import com.redhat.ceylon.compiler.typechecker.model.Method;
import com.redhat.ceylon.compiler.typechecker.model.Setter;
import com.redhat.ceylon.compiler.typechecker.tree.Tree;
import com.redhat.ceylon.eclipse.code.editor.Util;

class ConvertToBlockProposal extends ChangeCorrectionProposal {
    
    final int offset; 
    final IFile file;
    
    ConvertToBlockProposal(int offset, IFile file, TextChange change) {
        super("Convert => to block", change, 10, CORRECTION);
        this.offset=offset;
        this.file=file;
    }
    
    @Override
    public void apply(IDocument document) {
        super.apply(document);
        Util.gotoLocation(file, offset);
    }

    static void addConvertToBlockProposal(IDocument doc,
            Collection<ICompletionProposal> proposals, IFile file,
            Tree.LazySpecifierExpression spec,
            Tree.Declaration decNode) {
            TextChange change = new DocumentChange("Convert To Block", doc);
            change.setEdit(new MultiTextEdit());
            Integer offset = spec.getStartIndex();
            String space;
            String spaceAfter;
            try {
                space = doc.getChar(offset-1)==' ' ? "" : " ";
                spaceAfter = doc.getChar(offset+2)==' ' ? "" : " ";
            }
            catch (BadLocationException e) {
                e.printStackTrace();
                return;
            }
            Declaration dm = decNode.getDeclarationModel();
            boolean isVoid = dm instanceof Setter ||
                    dm instanceof Method && ((Method) dm).isDeclaredVoid();
            change.addEdit(new ReplaceEdit(offset, 2, space + (isVoid?"{":"{ return") + spaceAfter));
            change.addEdit(new InsertEdit(decNode.getStopIndex()+1, " }"));
            proposals.add(new ConvertToBlockProposal(offset + space.length() + 2 , file, change));
    }
    
}
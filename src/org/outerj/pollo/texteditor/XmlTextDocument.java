package org.outerj.pollo.texteditor;

import org.outerj.pollo.texteditor.action.RedoAction;
import org.outerj.pollo.texteditor.action.UndoAction;

import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import javax.swing.undo.UndoManager;

public class XmlTextDocument extends SyntaxDocument
{
    protected UndoManager undoManager;
    protected UndoAction undoAction;
    protected RedoAction redoAction;

    public XmlTextDocument()
    {
        super();
        setTokenMarker(new XMLTokenMarker());
        undoManager = new UndoManager();
        undoAction = new UndoAction(this);
        redoAction = new RedoAction(this);
    }

    public void stopUndo()
    {
        removeUndoableEditListener(undoManager);
    }

    public void startUndo()
    {
        undoManager.discardAllEdits();
        addUndoableEditListener(undoManager);
    }

    public void undo()
    {
        undoManager.undo();
    }

    public void redo()
    {
        undoManager.redo();
    }

    public UndoAction getUndoAction()
    {
        return undoAction;
    }

    public RedoAction getRedoAction()
    {
        return redoAction;
    }

    /**
     * Tries to get the encoding defined in the XML file. If not found,
     * null is returned.
     */
    public String getEncoding()
    {
        Segment seg = new Segment();
        int length = getLength();
        try
        {
            getText(0, length, seg);
        }
        catch (BadLocationException e)
        {
            return null;
        }

        StringBuffer encoding = new StringBuffer(10);

        // check if the document starts with '<?xml'
        if (!isWord("<?xml", seg.array, 0, seg.count))
        {
            return null;
        }
        
        // skip whitespace
        int r = 5;
        if ((r = skipWhiteSpace(seg.array, r, seg.count)) == -1)
            return null;

        // now the word version should follow
        if (!isWord("version", seg.array, r, seg.count))
        {
            return null;
        }

        r += "version".length();

        // skip whitespace
        if ((r = skipWhiteSpace(seg.array, r, seg.count)) == -1)
            return null;

        // now we should have '='
        if (!isWord("=", seg.array, r, seg.count))
            return null;

        r++;

        // skip whitespace
        if ((r = skipWhiteSpace(seg.array, r, seg.count)) == -1)
            return null;

        char q = seg.array[r];
        if (q != '"' && q != '\'')
            return null;

        r++;

        // now we should have 1.0
        if (!isWord("1.0" + q, seg.array, r, seg.count))
            return null;

        r += 4;

        // skip whitespace
        if ((r = skipWhiteSpace(seg.array, r, seg.count)) == -1)
            return null;

        // now we should have 'encoding'
        if (!isWord("encoding", seg.array, r, seg.count))
            return null;

        r += "encoding".length();

        // skip whitespace
        if ((r = skipWhiteSpace(seg.array, r, seg.count)) == -1)
            return null;

        // now we should have '='
        if (!isWord("=", seg.array, r, seg.count))
            return null;

        r++;

        // skip whitespace
        if ((r = skipWhiteSpace(seg.array, r, seg.count)) == -1)
            return null;

        q = seg.array[r];
        if (q != '"' && q != '\'')
            return null;

        r++;


        int m = r;
        while (m < seg.count && seg.array[m] != q)
        {
            if (    (seg.array[m] >= 'a' && seg.array[m] <= 'z') ||
                    (seg.array[m] >= 'A' && seg.array[m] <= 'Z') ||
                    (seg.array[m] >= '0' && seg.array[m] <= '9') ||
                    seg.array[m] == '.' || seg.array[m] == '-' ||
                    seg.array[m] == '_')
            {
                encoding.append(seg.array[m]);
                m++;
            }
            else
            {
                return null;
            }
        }

        if (m < seg.count && seg.array[m] == q)
        {
            //System.out.println("Detected encoding for saving file: " + encoding);
            return encoding.toString();
        }

        return null;
    }

    private boolean isXmlSpace(char c)
    {
        if (c == ' ' || c == '\t')
            return true;
        else
            return false;
    }

    private boolean isWord(String word, char [] text, int offset, int count)
    {
        for (int i = 0; i < word.length(); i++)
        {
            if (offset + i >= count)
                return false;

            if (!(text[offset + i] == word.charAt(i)))
                return false;
        }
        return true;
    }

    private int skipWhiteSpace(char [] text, int offset, int count)
    {
        int i = offset;
        while (isXmlSpace(text[i]))
        {
            i++;
            if (i >= count)
                return -1;
        }
        return i;
    }
}

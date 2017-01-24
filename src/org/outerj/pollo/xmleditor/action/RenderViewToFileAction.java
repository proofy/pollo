package org.outerj.pollo.xmleditor.action;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;
import org.outerj.pollo.util.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * An action that renders the selected view to an image and
 * stores it in a file.
 *
 * @author Bruno Dumon
 */
public class RenderViewToFileAction extends AbstractNodeAction
{
    protected JFileChooser fileChooser;
    protected static final ResourceManager resourceManager = ResourceManager.getManager(RenderViewToFileAction.class);

    public RenderViewToFileAction(XmlEditor xmlEditor)
    {
        super(xmlEditor);
        resourceManager.configureAction(this);
    }

    public void actionPerformed(ActionEvent event)
    {
        View selectedView = xmlEditor.getSelectionInfo().getSelectedNodeView();
        if (selectedView != null)
        {
            // create the image
            int width = selectedView.getWidth() + 1;
            int height = selectedView.getHeight() + 2;
            BufferedImage image;
            try
            {
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }
            catch (OutOfMemoryError e)
            {
                JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
                        "Not enough memory to create image.");  
                return;
            }

            Graphics2D gr = image.createGraphics();
            gr.setClip(0, 0, width, height);

            // unselect the selected view, so that the selection highlight won't be rendered
            xmlEditor.getSelectionInfo().unselect();

            // draw the background
            Color backgroundColor = xmlEditor.getDisplaySpec().getBackgroundColor();
            gr.setColor(backgroundColor);
            gr.fillRect(0, 0, width, height);

            // draw the view
            selectedView.paint(gr, 0, 0);

            // ask user for a filename
            if (fileChooser == null)
                fileChooser = new JFileChooser();
            int returnVal = fileChooser.showSaveDialog(xmlEditor.getTopLevelAncestor());
            if(returnVal != JFileChooser.APPROVE_OPTION)
                return;

            File file = fileChooser.getSelectedFile();
            
            // save it as a jpeg (png would be better but not supported by jdk 1.3)
            OutputStream ostream = null;
            try
            {
                ostream = new FileOutputStream(file);
                ByteArrayOutputStream bstream = new ByteArrayOutputStream();
                JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(bstream);
                JPEGEncodeParam params = JPEGCodec.getDefaultJPEGEncodeParam(image);
                params.setQuality(1f, true);
                jpegEncoder.encode(image, params);
                ostream.write(bstream.toByteArray());
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
                        "Error storing image: " + e.getMessage());  
            }
            finally
            {
                if (ostream != null)
                {
                    try
                    {
                        ostream.close();
                    }
                    catch (Exception e)
                    {
                        System.out.println("[RenderViewToFileAction] Error closing file.");
                        e.printStackTrace();
                    }
                }

            }
        }
    }

}

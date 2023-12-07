import javax.swing.*;
import java.awt.*;

class ImagePanel extends JPanel {
    private Image backgroundImage;
    public ImagePanel(String imagePath) {
        this.backgroundImage = new ImageIcon(imagePath).getImage();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, this);
    }
}
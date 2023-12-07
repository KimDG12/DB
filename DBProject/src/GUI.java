import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends JFrame{
    private static GUI instance;
    public GUI() {
        instance = this;
        setTitle("User Authentication");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }
    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        JButton logInButton = new JButton("로그인");
        logInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logIn();
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(logInButton, constraints);
        JButton signUpButton = new JButton("회원가입");
        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signUp();
            }
        });
        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(signUpButton, constraints);
        add(panel);
    }
    private void logIn() {
        LogInFrame logInFrame = new LogInFrame();
        logInFrame.setVisible(true);
        dispose();
    }
    private void signUp() {
        SignUpFrame signUpFrame = new SignUpFrame();
        signUpFrame.setVisible(true);
        dispose();
    }
    public static void showGUI() {
        if (instance != null) {
            instance.setVisible(true);
        }
    }
}
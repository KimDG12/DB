import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LogInFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LogInFrame() {
        setTitle("Log In");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                GUI.showGUI(); // 창을 닫을 때 메인 GUI 다시 표시
            }
        });
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        JLabel usernameLabel = new JLabel("아이디:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(usernameLabel, constraints);
        usernameField = new JTextField(15);
        constraints.gridx = 1;
        panel.add(usernameField, constraints);
        JLabel passwordLabel = new JLabel("비밀번호:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(passwordLabel, constraints);
        passwordField = new JPasswordField(15);
        constraints.gridx = 1;
        panel.add(passwordField, constraints);
        JButton logInButton = new JButton("로그인");
        logInButton.addActionListener(e -> logIn());
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(logInButton, constraints);
        add(panel);
    }

    private void logIn() {
        String username = usernameField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        try {
            String userId = validateUser(username, password);
            if (userId != null) {
                JOptionPane.showMessageDialog(this, "로그인 성공");
                openMainApplication(userId); // 사용자 ID를 전달
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "로그인 실패. 다시 확인해주세요.");
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
    }

    private void openMainApplication(String userId) {
        String username = usernameField.getText();
        MainApplicationFrame mainFrame = new MainApplicationFrame(username);
        mainFrame.setVisible(true);
        dispose();

    }

    private String validateUser(String username, String password) throws Exception {
        String driver = "oracle.jdbc.OracleDriver";
        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        String user = "name";
        String dbPassword = "pw";
        Class.forName(driver);
        Connection db = DriverManager.getConnection(url, user, dbPassword);
        String sql = "SELECT * FROM MEMBER WHERE user_id = ? AND password = ?";
        try (PreparedStatement preparedStatement = db.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("user_id");
            }
        } finally {
            db.close();
        }
        return null;
    }
}
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SignUpFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField nameField;
    private JTextField accountIdField;
    private JPasswordField confirmPasswordField;

    public SignUpFrame() {
        setTitle("Sign Up");
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
        JLabel confirmPasswordLabel = new JLabel("2차 비밀번호:");
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(confirmPasswordLabel, constraints);
        confirmPasswordField = new JPasswordField(15);
        constraints.gridx = 1;
        panel.add(confirmPasswordField, constraints);
        JLabel nameLabel = new JLabel("닉네임:");
        constraints.gridx = 0;
        constraints.gridy = 3;
        panel.add(nameLabel, constraints);
        nameField = new JTextField(15);
        constraints.gridx = 1;
        panel.add(nameField, constraints);
        JLabel accountIdLabel = new JLabel("계좌번호(없는 계좌번호이면 저장안됨):");
        constraints.gridx = 0;
        constraints.gridy = 4;
        panel.add(accountIdLabel, constraints);
        accountIdField = new JTextField(15);
        constraints.gridx = 1;
        panel.add(accountIdField, constraints);
        JButton signUpButton = new JButton("회원가입");
        signUpButton.addActionListener(e -> signUp());
        constraints.gridx = 0;
        constraints.gridy = 5;
        panel.add(signUpButton, constraints);
        add(panel);
    }
    private void signUp() {
        String username = usernameField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        String name = nameField.getText();
        String accountId = accountIdField.getText();
        char[] passwordchar2 = confirmPasswordField.getPassword();
        String password2 = new String(passwordchar2);
        try {
            if (areAccountAndUserIdSameInDatabase(accountId, username)) {
                insertUserData(username, password, name, accountId, password2);
                JOptionPane.showMessageDialog(this, "회원가입 성공");
                GUI.showGUI();
            } else {
                JOptionPane.showMessageDialog(this, "정보가 일치하지 않습니다. 다시 시도해주세요.");
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "에러.");
        }
        dispose();
    }
    private boolean areAccountAndUserIdSameInDatabase(String accountId, String username) throws Exception {
        String driver = "oracle.jdbc.OracleDriver";
        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        String user = "name";
        String dbPassword = "pw";
        Class.forName(driver);
        try (Connection connection = DriverManager.getConnection(url, user, dbPassword)) {
            String sql = "SELECT * FROM ACCOUNT WHERE account_id = ? AND user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, accountId);
                preparedStatement.setString(2, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        }
    }

    public static void insertUserData(String username, String password, String name, String accountId, String password2) throws Exception {
        String driver = "oracle.jdbc.OracleDriver";
        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        String user = "name";
        String dbPassword = "pw";
        Class.forName(driver);
        Connection db = DriverManager.getConnection(url, user, dbPassword);
        String sql = "INSERT INTO MEMBER (user_id, password, name, account_id, password2) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstmt = db.prepareStatement(sql);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, name);
        pstmt.setString(4, accountId);
        pstmt.setString(5, password2);
        pstmt.execute();
        db.close();
    }
}

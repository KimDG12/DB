import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class MainApplicationFrame extends JFrame {
    private String userId;
    private String accountId;
    private JLabel balanceLabel;
    private JLabel userLabel;
    private JButton depositButton;
    private JButton withdrawButton;
    private JButton transferButton;
    private JButton historyButton;
    private JButton logoutButton;
    private String jdbcUrl = "jdbc:oracle:thin:@localhost:1521:XE";
    private String dbUsername = "name";
    private String dbPassword = "pw";
    private String getAccountName(String accountId) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            String sql = "SELECT m.name FROM member m JOIN account a ON m.account_id = a.account_id WHERE a.account_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, accountId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("name");
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "데이터베이스에서 사용자 이름을 가져오는 중 오류가 발생했습니다.");
        }
        return null;
    }
    public MainApplicationFrame(String userId) {
        this.userId = userId;
        this.accountId = getAccountId(userId);
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            connection.setAutoCommit(false); // 자동 커밋 비활성화
        } catch (SQLException e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터베이스 연결 중 오류가 발생했습니다.");
        }
        setTitle("Main Application");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        updateBalanceLabel();
    }
    private void initUI() {
        ImagePanel imagePanel = new ImagePanel("C:/Users/Owner-39/Desktop/2001571/DBProject/image/main.jpg");
        imagePanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        // 현재 사용중인 사용자 닉네임 출력
        userLabel = new JLabel("사용자: " + getAccountName(accountId));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 4;
        imagePanel.add(userLabel, constraints);
        constraints.gridwidth = 1;
        // 현재 사용중인 계좌 번호 출력
        String accountInfo = "계좌번호: " + accountId;
        JLabel accountLabel = new JLabel(accountInfo);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 4;
        imagePanel.add(accountLabel, constraints);
        constraints.gridwidth = 1;
        // 현재 돈 표시 레이블
        balanceLabel = new JLabel("현재 돈: ");
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 4; // 4열을 차지하도록 설정
        imagePanel.add(balanceLabel, constraints);
        constraints.gridwidth = 1; // 다음 요소부터는 다시 1열로 설정
        // 입금 버튼
        depositButton = new JButton("입금");
        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDepositDialog();
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 5;
        imagePanel.add(depositButton, constraints);
        // 출금 버튼
        withdrawButton = new JButton("출금");
        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showWithdrawDialog();
            }
        });
        constraints.gridx = 1;
        imagePanel.add(withdrawButton, constraints);
        // 이체 버튼
        transferButton = new JButton("이체");
        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTransfer();
            }
        });
        constraints.gridx = 2;
        imagePanel.add(transferButton, constraints);
        // 내역 버튼
        historyButton = new JButton("내역");
        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTransactionHistory();
            }
        });
        constraints.gridx = 3;
        imagePanel.add(historyButton, constraints);
        logoutButton = new JButton("로그아웃");
        logoutButton.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {handleLogout(); }
        });
        constraints.gridx = 1;
        constraints.gridy = 6;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.PAGE_END;
        imagePanel.add(logoutButton, constraints);

        add(imagePanel);
    }
    private String getAccountId(String userId) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            String sql = "SELECT account_id FROM account WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("account_id");
                    }
                }
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터베이스에서 계좌 ID를 가져오는 중 오류가 발생했습니다.");
        }
        return null;
    }
    private void handleDeposit(double depositAmount, String memo) {
        updateBalanceInDatabase(depositAmount);
        recordTransaction(depositAmount, memo, "입금");
        updateBalanceLabel();
    }
    private void handleWithdraw(double withdrawAmount, String memo) {
        updateBalanceInDatabase(-withdrawAmount);
        recordTransaction(-withdrawAmount, memo, "출금");
        updateBalanceLabel();
    }
    private void handleTransfer() {
        JTextField targetAccountIdField = new JTextField();
        JTextField transferAmountField = new JTextField();
        JTextField transferMemoField = new JTextField();
        JPanel transferInfoPanel = new JPanel(new GridLayout(0, 2));
        transferInfoPanel.add(new JLabel("받을 계좌 ID:"));
        transferInfoPanel.add(targetAccountIdField);
        transferInfoPanel.add(new JLabel("이체할 금액:"));
        transferInfoPanel.add(transferAmountField);
        transferInfoPanel.add(new JLabel("메모:"));
        transferInfoPanel.add(transferMemoField);
        int transferInfoResult = JOptionPane.showConfirmDialog(this, transferInfoPanel, "이체 정보 입력", JOptionPane.OK_CANCEL_OPTION);
        if (transferInfoResult != JOptionPane.OK_OPTION) {
            // 사용자가 확인을 누르지 않거나 취소했을 경우 이체 중단
            return;
        }
        String targetAccountId = targetAccountIdField.getText();
        double transferAmount = isValidAmount(transferAmountField.getText()) ? Double.parseDouble(transferAmountField.getText()) : 0.0;
        String transferMemo = transferMemoField.getText();
        // 2차 비밀번호 확인을 위한 다이얼로그 추가
        JPasswordField secondPasswordField = new JPasswordField();
        JPanel passwordPanel = new JPanel(new GridLayout(0, 1));
        passwordPanel.add(new JLabel("2차 비밀번호:"));
        passwordPanel.add(secondPasswordField);
        int passwordResult = JOptionPane.showConfirmDialog(this, passwordPanel, "이체를 위한 2차 비밀번호 확인", JOptionPane.OK_CANCEL_OPTION);
        if (passwordResult != JOptionPane.OK_OPTION) {
            // 사용자가 확인을 누르지 않거나 취소했을 경우 이체 중단
            return;
        }
        char[] secondPasswordChars = secondPasswordField.getPassword();
        String secondPassword = new String(secondPasswordChars);
        // 2차 비밀번호 확인
        if (!isValidSecondPassword(targetAccountId, secondPassword)) {
            JOptionPane.showMessageDialog(this, "2차 비밀번호가 올바르지 않습니다. 이체를 중단합니다.");
            return;
        }
        // 이제 2차 비밀번호가 올바르다고 가정하고 이어서 로직을 진행합니다.
        if (isValidAccountId(targetAccountId) && isValidAmount(String.valueOf(transferAmount))) {
            // 이체 대상 계좌에 대한 처리
            String targetAccountOwnerName = getAccountName(targetAccountId);
            if (targetAccountOwnerName != null) {
                int confirmResult = JOptionPane.showConfirmDialog(this, "이체 대상: " + targetAccountOwnerName + "\n계속 진행하시겠습니까?", "이체 대상 확인", JOptionPane.YES_NO_OPTION);
                if (confirmResult == JOptionPane.YES_OPTION) {
                    updateBalanceInDatabaseForTargetAccount(targetAccountId, transferAmount);
                    recordTransactionForTargetAccount(targetAccountId, transferAmount, transferMemo, "입금");
                    // 나의 계좌에 대한 처리
                    updateBalanceInDatabase(-transferAmount);
                    recordTransaction(-transferAmount, transferMemo, "출금");
                    // UI 갱신
                    updateBalanceLabel();
                } else {
                    JOptionPane.showMessageDialog(this, "이체를 취소합니다.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "계좌 소유자 정보를 가져오는 중 오류가 발생했습니다.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "유효하지 않은 입력입니다. 유효한 숫자와 계좌 ID를 입력하세요.");
        }
    }
    private void handleLogout() {
        dispose();
        new GUI().setVisible(true);
    }
    private void updateBalanceInDatabase(double amount) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            String sql = "UPDATE account SET balance = balance + ? WHERE account_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setDouble(1, amount);
                preparedStatement.setString(2, accountId);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터베이스에서 현재 돈을 업데이트하는 중 오류가 발생했습니다.");
        }
    }
    private boolean isValidSecondPassword(String targetAccountId, String secondPassword) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            String sql = "SELECT password2 FROM member WHERE account_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, targetAccountId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String actualSecondPassword = resultSet.getString("password2");
                        return actualSecondPassword.equals(secondPassword);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터베이스에서 2차 비밀번호를 확인하는 중 오류가 발생했습니다.");
        }
        return false;
    }
    private boolean isValidAmount(String amountString) {
        try {
            double amount = Double.parseDouble(amountString);
            return amount >= 0; // 금액은 음수가 될 수 없습니다.
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private boolean isValidAccountId(String accountId) {
        return accountId != null && !accountId.isEmpty();
    }
    private void updateBalanceLabel() {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            String sql = "SELECT balance FROM account WHERE account_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, accountId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        double currentBalance = resultSet.getDouble("balance");
                        balanceLabel.setText("현재 돈: " + currentBalance);
                    }
                }
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터베이스에서 현재 돈을 가져오는 중 오류가 발생했습니다.");
        }
    }
    private void updateBalanceInDatabaseForTargetAccount(String targetAccountId, double amount) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            String sql = "UPDATE account SET balance = balance + ? WHERE account_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setDouble(1, amount);
                preparedStatement.setString(2, targetAccountId);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "데이터베이스에서 대상 계좌의 현재 돈을 업데이트하는 중 오류가 발생했습니다.");
        }
    }
    private void recordTransaction(double amount, String memo, String transactionType) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            connection.setAutoCommit(false);
            String sql = "INSERT INTO transaction (account_id, amount, memo, tr_type) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, accountId);
                preparedStatement.setDouble(2, amount);
                preparedStatement.setString(3, memo);
                preparedStatement.setString(4, transactionType);
                preparedStatement.executeUpdate();
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터베이스에 이체 내역을 기록하는 중 오류가 발생했습니다.");
        }
    }
    private void recordTransactionForTargetAccount(String targetAccountId, double amount, String memo, String transactionType) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            connection.setAutoCommit(false);
            String sqlTargetAccount = "INSERT INTO transaction (account_id, amount, memo, tr_type) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatementTarget = connection.prepareStatement(sqlTargetAccount)) {
                preparedStatementTarget.setString(1, targetAccountId);
                preparedStatementTarget.setDouble(2, amount);
                preparedStatementTarget.setString(3, memo);
                preparedStatementTarget.setString(4, transactionType);
                preparedStatementTarget.executeUpdate();
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "데이터베이스에 대상 계좌의 이체 내역을 기록하는 중 오류가 발생했습니다.");
        }
    }
    private void showDepositDialog() {
        JTextField amountField = new JTextField(10);
        JTextField memoField = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("금액:"));
        panel.add(amountField);
        panel.add(new JLabel("메모:"));
        panel.add(memoField);
        int result = JOptionPane.showConfirmDialog(this, panel, "입금", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            double amount = isValidAmount(amountField.getText()) ? Double.parseDouble(amountField.getText()) : 0.0;
            String memo = memoField.getText();
            handleDeposit(amount, memo);
        }
    }
    private void showWithdrawDialog() {
        JTextField amountField = new JTextField(10);
        JTextField memoField = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("금액:"));
        panel.add(amountField);
        panel.add(new JLabel("메모:"));
        panel.add(memoField);
        int result = JOptionPane.showConfirmDialog(this, panel, "출금", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            double amount = isValidAmount(amountField.getText()) ? Double.parseDouble(amountField.getText()) : 0.0;
            String memo = memoField.getText();
            handleWithdraw(amount, memo);
        }
    }
    private void showTransactionHistory() {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM transaction WHERE account_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, accountId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    StringBuilder transactionHistory = new StringBuilder();
                    while (resultSet.next()) {
                        String transactionType = resultSet.getString("tr_type");
                        double amount = resultSet.getDouble("amount");
                        String memo = resultSet.getString("memo");
                        transactionHistory.append(transactionType)
                                .append(" - 금액: ").append(amount)
                                .append(", 메모: ").append(memo)
                                .append("\n");
                    }
                    JOptionPane.showMessageDialog(this, transactionHistory.toString(), "거래 내역", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "거래 내역을 불러오는 중 오류가 발생했습니다.");
        }
    }
}
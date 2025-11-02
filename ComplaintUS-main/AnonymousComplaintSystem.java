import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Random;
import com.formdev.flatlaf.FlatDarkLaf;

public class AnonymousComplaintSystem extends JFrame {
    private JTextArea complaintArea, statusArea;
    private JComboBox<String> categoryBox, priorityBox;
    private JTextField trackingField;
    private Connection conn;
    private boolean isAdminLoggedIn = false;

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/complaint_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root123";

    public AnonymousComplaintSystem() {
        setTitle("ComplaintUS - Anonymous Complaint System");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try { conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS); }
        catch (SQLException e) { JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage()); }

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("ComplaintUS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 30));
        main.add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("📝 Submit", createCard(submitPanel()));
        tabs.add("🔍 Track",  createCard(trackPanel()));
        tabs.add("⚙️ Admin",  createCard(adminPanel()));
        main.add(tabs, BorderLayout.CENTER);

        add(main);
        setVisible(true);
    }

    private JPanel createCard(JPanel inner) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(49,51,53));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(25,25,25,25),
            BorderFactory.createLineBorder(new Color(70,70,70),1,true)
        ));
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ---------- Submit ----------
    private JPanel submitPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10,10,10,10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel h = new JLabel("Submit Anonymous Complaint", SwingConstants.CENTER);
        h.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.gridx=0; g.gridy=0; g.gridwidth=2; p.add(h,g);

        g.gridwidth=1; g.gridy++;
        p.add(new JLabel("Category:"),g); g.gridx=1;
        categoryBox=new JComboBox<>(new String[]{"Academic","Infrastructure","Faculty","Harassment","Other"});
        p.add(categoryBox,g);

        g.gridx=0; g.gridy++;
        p.add(new JLabel("Priority:"),g); g.gridx=1;
        priorityBox=new JComboBox<>(new String[]{"Low","Medium","High","Critical"});
        p.add(priorityBox,g);

        g.gridx=0; g.gridy++;
        p.add(new JLabel("Complaint:"),g); g.gridx=1;
        complaintArea=new JTextArea(5,25);
        p.add(new JScrollPane(complaintArea),g);

        g.gridy++; g.gridx=0; g.gridwidth=2;
        JButton b=new JButton("Submit Complaint"); styleButton(b);
        b.addActionListener(e->submitComplaint()); p.add(b,g);
        return p;
    }

    // ---------- Track ----------
    private JPanel trackPanel() {
        JPanel p=new JPanel(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints();
        g.insets=new Insets(10,10,10,10); g.fill=GridBagConstraints.HORIZONTAL;

        JLabel h=new JLabel("Track Your Complaint",SwingConstants.CENTER);
        h.setFont(new Font("Segoe UI",Font.BOLD,20));
        g.gridx=0; g.gridy=0; g.gridwidth=3; p.add(h,g);

        g.gridwidth=1; g.gridy++;
        p.add(new JLabel("Tracking ID:"),g); g.gridx=1;
        trackingField=new JTextField(15); p.add(trackingField,g);

        g.gridx=2; JButton nxt=new JButton("Next");
        styleButton(nxt); nxt.addActionListener(e->showNextComplaint()); p.add(nxt,g);

        g.gridy++; g.gridx=0; g.gridwidth=3;
        JButton check=new JButton("Check Status");
        styleButton(check); check.addActionListener(e->trackComplaint()); p.add(check,g);

        g.gridy++;
        statusArea=new JTextArea(5,30); statusArea.setEditable(false);
        p.add(new JScrollPane(statusArea),g);
        return p;
    }

    // ---------- Admin ----------
    private JPanel adminPanel() {
        if(!isAdminLoggedIn) return adminLoginPanel();
        JPanel p=new JPanel(new BorderLayout(10,10));

        JLabel h=new JLabel("Admin Dashboard",SwingConstants.CENTER);
        h.setFont(new Font("Segoe UI",Font.BOLD,22)); p.add(h,BorderLayout.NORTH);

        JTable table=new JTable(); JScrollPane sp=new JScrollPane(table);
        p.add(sp,BorderLayout.CENTER);

        JPanel bottom=new JPanel();
        JButton view=new JButton("View Details"); styleButton(view);
        JButton update=new JButton("Update Status"); styleButton(update);
        JButton del=new JButton("Delete"); styleButton(del);
        JButton ref=new JButton("Refresh"); styleButton(ref);
        JButton logout=new JButton("Logout"); styleButton(logout);

        view.addActionListener(e->{
            int r=table.getSelectedRow();
            if(r>=0) showComplaintDetails((String)table.getValueAt(r,0));
            else JOptionPane.showMessageDialog(this,"Select a complaint first.");
        });

        update.addActionListener(e->{
            int r=table.getSelectedRow();
            if(r>=0) updateComplaintStatus((String)table.getValueAt(r,0));
            else JOptionPane.showMessageDialog(this,"Select a complaint first.");
        });

        del.addActionListener(e->{
            int r=table.getSelectedRow();
            if(r>=0){
                String id=(String)table.getValueAt(r,0);
                int c=JOptionPane.showConfirmDialog(this,"Delete complaint "+id+"?","Confirm",JOptionPane.YES_NO_OPTION);
                if(c==JOptionPane.YES_OPTION) deleteComplaint(id);
            }else JOptionPane.showMessageDialog(this,"Select a complaint first.");
        });

        ref.addActionListener(e->loadComplaints(table));
        logout.addActionListener(e->{isAdminLoggedIn=false;refreshUI();});

        bottom.add(view); bottom.add(update); bottom.add(del);
        bottom.add(ref); bottom.add(logout);
        p.add(bottom,BorderLayout.SOUTH);

        loadComplaints(table);
        return p;
    }

    private JPanel adminLoginPanel(){
        JPanel p=new JPanel(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints();
        g.insets=new Insets(10,10,10,10); g.fill=GridBagConstraints.HORIZONTAL;

        JLabel h=new JLabel("Admin Login",SwingConstants.CENTER);
        h.setFont(new Font("Segoe UI",Font.BOLD,22));
        g.gridx=0; g.gridy=0; g.gridwidth=2; p.add(h,g);

        g.gridwidth=1; g.gridy++; p.add(new JLabel("Username:"),g); g.gridx=1;
        JTextField u=new JTextField(); p.add(u,g);

        g.gridx=0; g.gridy++; p.add(new JLabel("Password:"),g); g.gridx=1;
        JPasswordField pw=new JPasswordField(); p.add(pw,g);

        g.gridy++; g.gridx=0; g.gridwidth=2;
        JButton b=new JButton("Login"); styleButton(b);
        b.addActionListener(e->{
            if(u.getText().equals("admin")&&new String(pw.getPassword()).equals("admin123")){
                isAdminLoggedIn=true; refreshUI();
            }else JOptionPane.showMessageDialog(this,"Invalid Credentials!");
        });
        p.add(b,g);
        return p;
    }

    // ---------- Common helpers ----------
    private void styleButton(JButton b){
        b.setFocusPainted(false);
        b.setBackground(new Color(70,120,230));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI",Font.BOLD,14));
        b.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ---------- DB functions ----------
    private void submitComplaint(){
        try{
            String id="CMP-"+new Random().nextInt(999999);
            String sql="INSERT INTO complaints(tracking_id,category,priority,complaint_text,status) VALUES(?,?,?,?,?)";
            PreparedStatement ps=conn.prepareStatement(sql);
            ps.setString(1,id);
            ps.setString(2,(String)categoryBox.getSelectedItem());
            ps.setString(3,(String)priorityBox.getSelectedItem());
            ps.setString(4,complaintArea.getText());
            ps.setString(5,"Pending");
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,"Complaint submitted!\nTracking ID: "+id);
            complaintArea.setText("");
        }catch(SQLException e){JOptionPane.showMessageDialog(this,e.getMessage());}
    }

    private void trackComplaint(){
        try{
            String id=trackingField.getText().trim();
            String sql="SELECT * FROM complaints WHERE tracking_id=?";
            PreparedStatement ps=conn.prepareStatement(sql);
            ps.setString(1,id); ResultSet rs=ps.executeQuery();
            if(rs.next())
                statusArea.setText("Complaint: "+rs.getString("complaint_text")+
                                   "\nStatus: "+rs.getString("status"));
            else statusArea.setText("No complaint found for ID: "+id);
        }catch(SQLException e){JOptionPane.showMessageDialog(this,e.getMessage());}
    }

    private void showNextComplaint(){
        try{
            String id=trackingField.getText().trim();
            String sql="SELECT tracking_id FROM complaints ORDER BY tracking_id";
            Statement st=conn.createStatement(); ResultSet rs=st.executeQuery(sql);
            boolean f=false;
            while(rs.next()){
                if(f){trackingField.setText(rs.getString(1));trackComplaint();return;}
                if(rs.getString(1).equals(id)) f=true;
            }
            JOptionPane.showMessageDialog(this,"No more complaints.");
        }catch(SQLException e){JOptionPane.showMessageDialog(this,e.getMessage());}
    }

    private void deleteComplaint(String id){
        try{
            PreparedStatement ps=conn.prepareStatement("DELETE FROM complaints WHERE tracking_id=?");
            ps.setString(1,id); ps.executeUpdate();
            JOptionPane.showMessageDialog(this,"Complaint "+id+" deleted.");
            refreshUI();
        }catch(SQLException e){JOptionPane.showMessageDialog(this,e.getMessage());}
    }

    private void updateComplaintStatus(String id){
        String[] options={"Pending","In Progress","Resolved","Closed"};
        String newStatus=(String)JOptionPane.showInputDialog(this,
            "Select new status for "+id+":","Update Status",
            JOptionPane.PLAIN_MESSAGE,null,options,"Pending");
        if(newStatus!=null){
            try{
                PreparedStatement ps=conn.prepareStatement("UPDATE complaints SET status=? WHERE tracking_id=?");
                ps.setString(1,newStatus); ps.setString(2,id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this,"Status updated to "+newStatus);
                refreshUI();
            }catch(SQLException e){JOptionPane.showMessageDialog(this,e.getMessage());}
        }
    }

    private void loadComplaints(JTable t){
        try{
            DefaultTableModel m=new DefaultTableModel(new String[]{"ID","Category","Priority","Status"},0);
            Statement st=conn.createStatement(); ResultSet rs=st.executeQuery("SELECT * FROM complaints");
            while(rs.next())
                m.addRow(new Object[]{rs.getString("tracking_id"),rs.getString("category"),rs.getString("priority"),rs.getString("status")});
            t.setModel(m);
        }catch(SQLException e){JOptionPane.showMessageDialog(this,e.getMessage());}
    }

    private void showComplaintDetails(String id){
        try{
            PreparedStatement ps=conn.prepareStatement("SELECT * FROM complaints WHERE tracking_id=?");
            ps.setString(1,id); ResultSet rs=ps.executeQuery();
            if(rs.next()){
                JTextArea ta=new JTextArea(10,40); ta.setEditable(false);
                ta.setText("Tracking ID: "+rs.getString("tracking_id")+
                           "\nCategory: "+rs.getString("category")+
                           "\nPriority: "+rs.getString("priority")+
                           "\nStatus: "+rs.getString("status")+
                           "\n\nComplaint:\n"+rs.getString("complaint_text"));
                JOptionPane.showMessageDialog(this,new JScrollPane(ta),"Complaint Details",JOptionPane.INFORMATION_MESSAGE);
            }
        }catch(SQLException e){JOptionPane.showMessageDialog(this,e.getMessage());}
    }

    // ---------- UI Refresh ----------
    private void refreshUI(){
        getContentPane().removeAll();
        JPanel main=new JPanel(new BorderLayout());
        JLabel t=new JLabel("ComplaintUS",SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI",Font.BOLD,30));
        main.add(t,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();
        tabs.add("Submit",createCard(submitPanel()));
        tabs.add("Track",createCard(trackPanel()));
        tabs.add("Admin",createCard(adminPanel()));
        main.add(tabs,BorderLayout.CENTER);
        add(main); revalidate(); repaint();
    }

    public static void main(String[] args){
        try{
            UIManager.put("Component.arc",12);
            UIManager.put("Button.arc",12);
            UIManager.put("TextComponent.arc",12);
            UIManager.put("Button.background",new Color(65,105,225));
            UIManager.put("Button.foreground",Color.WHITE);
            UIManager.setLookAndFeel(new FlatDarkLaf());
        }catch(Exception e){e.printStackTrace();}
        SwingUtilities.invokeLater(AnonymousComplaintSystem::new);
    }
}

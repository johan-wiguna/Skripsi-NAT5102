import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class MainFrame extends JFrame implements ActionListener {
    JButton btnDataset1, btnDataset2;
    JLabel lblSelectDataset;
    GridBagConstraints c = new GridBagConstraints();
    
    public MainFrame() {
        this.setTitle("Hand Signature Validator");
        this.setLayout(new GridBagLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        this.setResizable(false);
        this.setSize(720, 540);
        this.setLocationRelativeTo(null);
        
        ImageIcon logo = new ImageIcon("C:/_My Files/Johan/GitHub/Skripsi-NAT5102/unpar.png");
        this.setIconImage(logo.getImage());
        
        lblSelectDataset = new JLabel("Select the desired dataset");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.ipady = 40;
        this.add(lblSelectDataset, c);
        
        c.gridwidth = 1;
        c.ipady = 0;
        
        btnDataset1 = new JButton("Dataset 1");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        this.add(btnDataset1, c);
        
        btnDataset2 = new JButton("Dataset 2");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        this.add(btnDataset2, c);
        
        btnDataset1.setFocusable(false);
        btnDataset2.setFocusable(false);
        
        btnDataset1.addActionListener(this);
        btnDataset2.addActionListener(this);
        
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == btnDataset1) {
            System.out.println("b1");
        } else if(e.getSource() == btnDataset2) {
            System.out.println("b2");
        }
    }
}

package runningman;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class RunningMan extends JFrame {

    public RunningMan() {
        initUI();
    }
    
    private void initUI() {
        add(new Board());
        setTitle("Running Man");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(735, 790);
        setLocationRelativeTo(null);
        setVisible(true);        
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                RunningMan ex = new RunningMan();
                ex.setVisible(true);
            }
        });
    }
}
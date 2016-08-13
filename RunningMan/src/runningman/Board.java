package runningman;

import entity.doRandom;
import entity.mapGenerator;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {

    private AudioClip soundtrack;
    private AudioClip win;
    private AudioClip lost;
    private AudioClip death;
    private AudioClip ding;

    private boolean ingame = false;
    private boolean dying = false;

    private final int blocksize = 48;
    private final int nrofblocks = 15;
    private final int scrsize = nrofblocks * blocksize;
    private final int pacanimdelay = 2;
    private final int runningmananimcount = 4;
    private final int maxghosts = 12;
    private final int runningmanspeed = 6;

    private int pacanimcount = pacanimdelay;
    private int pacanimdir = 1;
    private int runningmananimpos = 0;
    private int nrofghosts;
    private int pacsleft, score;
    private int[] dx, dy;
    private int[] ghostx, ghosty, ghostdx, ghostdy, ghostspeed;
    
    private Image bg;
    private Image ghost;
    private Image heart;
    private Image diamondImg;
    private Image runningman2up, runningman2left, runningman2right, runningman2down;
    private Image runningman3up, runningman3down, runningman3left, runningman3right;
    private Image runningman4up, runningman4down, runningman4left, runningman4right;

    private int runningmanx, runningmany, runningmandx, runningmandy;
    private int reqdx, reqdy, viewdx, viewdy;
    
    private final short diamond[] = {
        16, 17, 23, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 46,
        47, 50, 51, 52, 56, 57, 58, 61, 62, 65, 67, 68, 71, 72, 73, 76, 77,
        80, 81, 82, 83, 86, 87, 88, 91, 92, 97, 98, 99, 100, 101, 102, 103,
        106, 107, 116, 117, 118, 121, 122, 131, 132, 133, 136, 137, 138,
        139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 151, 152, 153,
        154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 166, 167, 168,
        169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 181, 182, 183,
        184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 199, 200, 201,
        202, 203, 204, 205, 206, 207, 208
    };

    private final int validspeeds[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private final int maxspeed = 6;

    private int currentspeed;
    private short[] screendata;
    private Timer timer;
    short RNDdaimond[] = new short[6];
    
    public Board() {
        RNDdaimond = doRandom.getRandom(diamond);
        
        loadImages();
        loadSounds();
        initVariables();
        
        addKeyListener(new TAdapter());
        setFocusable(true);
        setDoubleBuffered(true);
        
    }

    private void initVariables() {
        screendata = new short[nrofblocks * nrofblocks];
        ghostx = new int[maxghosts];
        ghostdx = new int[maxghosts];
        ghosty = new int[maxghosts];
        ghostdy = new int[maxghosts];
        ghostspeed = new int[maxghosts];
        
        dx = new int[4];
        dy = new int[4];
        
        timer = new Timer(40, this);
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        initGame();
    }

    private void doAnim() {
        pacanimcount--;
        if (pacanimcount <= 0) {
            pacanimcount = pacanimdelay;
            runningmananimpos = runningmananimpos + pacanimdir;
            if (runningmananimpos == (runningmananimcount - 1) || runningmananimpos == 0) {
                pacanimdir = -pacanimdir;
            }
        }
    }

    private void playGame(Graphics2D g2d) {
        if (dying) {
            death();
            soundtrack.stop();
            death.play();
            soundtrack.loop();
        } 
        else {
            moveRunningMan();
            drawRunningMan(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(50, scrsize / 2 - 30, scrsize - 100, 50);
        g2d.setColor(Color.white);
        g2d.drawRect(50, scrsize / 2 - 30, scrsize - 100, 50);

        String s = "Press S to start.";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (scrsize - metr.stringWidth(s)) / 2, scrsize / 2);
    }

    private void drawScore(Graphics2D g) {
        int i;
//        String s;
//        g.setFont(smallfont);
//        g.setColor(new Color(96, 128, 255));
//        s = "Score: " + score;
//        g.drawString(s, scrsize / 2 + 96, scrsize + 16);

        for (i = 0; i < pacsleft; i++) {
            g.drawImage(heart, i * 28 + 8, scrsize + 1, this);
        }
    }

    private void checkMaze() {
        boolean finished = true;
        for (int j : RNDdaimond){
            if ((screendata[j] & 48) != 0) {
                finished = false;
            }
        }

        if (finished) {
            win.play();
            ingame = false;
            continueLevel();
            loadImages2();
        }
    }

    private void death() {
        pacsleft--;
        if (pacsleft == 0) {
            lost.play();
            ingame = false;
        }
        continueLevel();
    }

    private void moveGhosts(Graphics2D g2d) {
        short i;
        int pos;
        int count;

        for (i = 0; i < nrofghosts; i++) {
            if (ghostx[i] % blocksize == 0 && ghosty[i] % blocksize == 0) {
                pos = ghostx[i] / blocksize + nrofblocks * (int) (ghosty[i] / blocksize);
                count = 0;
                
                if ((screendata[pos] & 1) == 0 && ghostdx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screendata[pos] & 2) == 0 && ghostdy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screendata[pos] & 4) == 0 && ghostdx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screendata[pos] & 8) == 0 && ghostdy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screendata[pos] & 15) == 15) {
                        ghostdx[i] = 0;
                        ghostdy[i] = 0;
                    } 
                    else {
                        ghostdx[i] = -ghostdx[i];
                        ghostdy[i] = -ghostdy[i];
                    }

                } 
                else {
                    count = (int) (Math.random() * count);
                    if (count > 3) {
                        count = 3;
                    }
                    
                    ghostdx[i] = dx[count];
                    ghostdy[i] = dy[count];
                }
            }
              Random r = new Random();
                short p = (short) r.nextInt(10);
            ghostx[i] = ghostx[i] + (ghostdx[i] * ghostspeed[i]);
            ghosty[i] = ghosty[i] + (ghostdy[i] * ghostspeed[i]);
            drawGhost(g2d, ghostx[i] + 1, ghosty[i] + 1);

            if (runningmanx > (ghostx[i] - 12) && runningmanx < (ghostx[i] + 12)
                    && runningmany > (ghosty[i] - 12) && runningmany < (ghosty[i] + 12)
                    && ingame) {
                dying = true;
            }
        }
    }

    private void drawGhost(Graphics2D g2d, int x, int y) {
        g2d.drawImage(ghost, x, y, this);
    }

    private void moveRunningMan() {
        int pos;
        short ch;

        if (reqdx == -runningmandx && reqdy == -runningmandy) {
            runningmandx = reqdx;
            runningmandy = reqdy;
            viewdx = runningmandx;
            viewdy = runningmandy;
        }

        if (runningmanx % blocksize == 0 && runningmany % blocksize == 0) {
            pos = runningmanx / blocksize + nrofblocks * (int) (runningmany / blocksize);
            ch = screendata[pos];

//           
//            if (((ch & 16) != 0)) { 
//                if(screendata[20] == screendata[RNDdaimond[0]] || screendata[21] == screendata[RNDdaimond[0]] || 
//                    screendata[22] == screendata[RNDdaimond[0]] ){
//                            screendata[pos] = (short) (ch & 15);
//                            score++;                        
//            }
//            }
            
            if ((ch & 16) != 0) { 
                screendata[pos] = (short) (ch & 15);
                for (int i : RNDdaimond) {
                    if (pos == i) {
                        ding.play();
                        if (nrofghosts < maxghosts) {
                            nrofghosts++;
                        }
                        if (currentspeed < maxspeed) {
                            currentspeed++;
                        }
                    }
                }
            }

            if (reqdx != 0 || reqdy != 0) {
                if (!((reqdx == -1 && reqdy == 0 && (ch & 1) != 0)
                        || (reqdx == 1 && reqdy == 0 && (ch & 4) != 0)
                        || (reqdx == 0 && reqdy == -1 && (ch & 2) != 0)
                        || (reqdx == 0 && reqdy == 1 && (ch & 8) != 0))) {
                    runningmandx = reqdx;
                    runningmandy = reqdy;
                    viewdx = runningmandx;
                    viewdy = runningmandy;
                }
            }

            if ((runningmandx == -1 && runningmandy == 0 && (ch & 1) != 0)
                    || (runningmandx == 1 && runningmandy == 0 && (ch & 4) != 0)
                    || (runningmandx == 0 && runningmandy == -1 && (ch & 2) != 0)
                    || (runningmandx == 0 && runningmandy == 1 && (ch & 8) != 0)) {
                runningmandx = 0;
                runningmandy = 0;
            }
        }
        runningmanx = runningmanx + runningmanspeed * runningmandx;
        runningmany = runningmany + runningmanspeed * runningmandy;
    }

    private void drawRunningMan(Graphics2D g2d) {
        if (viewdx == -1) {
            drawPacnanLeft(g2d);
        } else if (viewdx == 1) {
            drawRunningManRight(g2d);
        } else if (viewdy == -1) {
            drawRunningManUp(g2d);
        } else {
            drawRunningManDown(g2d);
        }
    }

    private void drawRunningManUp(Graphics2D g2d) {
        switch (runningmananimpos) {
            case 1:
                g2d.drawImage(runningman2up, runningmanx + 1, runningmany + 1, this);
                break;
            case 2:
                g2d.drawImage(runningman3up, runningmanx + 1, runningmany + 1, this);
                break;
            case 3:
                g2d.drawImage(runningman4up, runningmanx + 1, runningmany + 1, this);
                break;
            default:
                g2d.drawImage(runningman2up, runningmanx + 1, runningmany + 1, this);
                break;
        }
    }

    private void drawRunningManDown(Graphics2D g2d) {
        switch (runningmananimpos) {
            case 1:
                g2d.drawImage(runningman2down, runningmanx + 1, runningmany + 1, this);
                break;
            case 2:
                g2d.drawImage(runningman3down, runningmanx + 1, runningmany + 1, this);
                break;
            case 3:
                g2d.drawImage(runningman4down, runningmanx + 1, runningmany + 1, this);
                break;
            default:
                g2d.drawImage(runningman2down, runningmanx + 1, runningmany + 1, this);
                break;
        }
    }

    private void drawPacnanLeft(Graphics2D g2d) {
        switch (runningmananimpos) {
            case 1:
                g2d.drawImage(runningman2left, runningmanx + 1, runningmany + 1, this);
                break;
            case 2:
                g2d.drawImage(runningman3left, runningmanx + 1, runningmany + 1, this);
                break;
            case 3:
                g2d.drawImage(runningman4left, runningmanx + 1, runningmany + 1, this);
                break;
            default:
                g2d.drawImage(runningman2left, runningmanx + 1, runningmany + 1, this);
                break;
        }
    }

    private void drawRunningManRight(Graphics2D g2d) {
        switch (runningmananimpos) {
            case 1:
                g2d.drawImage(runningman2right, runningmanx + 1, runningmany + 1, this);
                break;
            case 2:
                g2d.drawImage(runningman3right, runningmanx + 1, runningmany + 1, this);
                break;
            case 3:
                g2d.drawImage(runningman4right, runningmanx + 1, runningmany + 1, this);
                break;
            default:
                g2d.drawImage(runningman2right, runningmanx + 1, runningmany + 1, this);
                break;
        }
    }

    private void drawMaze(Graphics2D g2d) {
        short i = 0;
        int x, y;

        for (y = 0; y < scrsize; y += blocksize) {
            for (x = 0; x < scrsize; x += blocksize) {     
                if ((screendata[i] & 16) != 0) {
                    for (int j : RNDdaimond){
                        if (i == j){                               
                            g2d.fillRect(x + 11, y + 11, 2, 2);
                            g2d.drawImage(diamondImg, x, y, this);
                        }
                    }                                       
                }                
                i++;
            }
        }
    }

    private void initGame() {
        pacsleft = 3;
        score = 0;
        initLevel();
        nrofghosts = 2;
        currentspeed = 1;
    }

    private void initLevel() {
        int i;
        for (i = 0; i < nrofblocks * nrofblocks; i++) {
            screendata[i] = mapGenerator.runningMap(i);
        }
        continueLevel();
    }

    private void continueLevel() {
        short i;
        int dx = 1;
        int random;  
        Random r = new Random();
        short p = (short) r.nextInt(10);
        for (i = 0; i < 12; i++) {
            ghosty[i] =  p * blocksize;
            ghostx[i] =  p * blocksize;
            ghostdy[i] = 0;
            ghostdx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentspeed + 1));

            if (random > currentspeed) {
                random = currentspeed;
            }
            ghostspeed[i] = validspeeds[random];
        }

        runningmanx = 5 * blocksize;
        runningmany = 5 * blocksize;
        runningmandx = 0;
        runningmandy = 0;
        reqdx = 0;
        reqdy = 0;
        viewdx = -1;
        viewdy = 0;
        dying = false;
    }

    private void loadImages() {
        diamondImg = new ImageIcon("images//diamond-1.gif").getImage();
        ghost = new ImageIcon("images//monster_1.gif").getImage();
        heart = new ImageIcon("images//life.png").getImage();
        runningman2up = new ImageIcon("images//up_1 (2).png").getImage();
        runningman3up = new ImageIcon("images//up_1 (1).png").getImage();
        runningman4up = new ImageIcon("images//up_1 (3).png").getImage();
        runningman2down = new ImageIcon("images//down_1 (2).png").getImage();
        runningman3down = new ImageIcon("images//down_1 (1).png").getImage();
        runningman4down = new ImageIcon("images//down_1 (3).png").getImage();
        runningman2left = new ImageIcon("images//left_1 (2).png").getImage();
        runningman3left = new ImageIcon("images//left_1 (1).png").getImage();
        runningman4left = new ImageIcon("images//left_1 (3).png").getImage();
        runningman2right = new ImageIcon("images//right_1 (2).png").getImage();
        runningman3right = new ImageIcon("images//right_1 (1).png").getImage();
        runningman4right = new ImageIcon("images//right_1 (3).png").getImage();
        bg = new ImageIcon("images//bg_1.png").getImage();
    }
    
    private void loadImages2() {
        ghost = new ImageIcon("images//monster_2.gif").getImage();
        runningman2up = new ImageIcon("images//up_2 (2).png").getImage();
        runningman3up = new ImageIcon("images//up_2 (1).png").getImage();
        runningman4up = new ImageIcon("images//up_2 (3).png").getImage();
        runningman2down = new ImageIcon("images//down_2 (1).png").getImage();
        runningman3down = new ImageIcon("images//down_2 (2).png").getImage();
        runningman4down = new ImageIcon("images//down_2 (3).png").getImage();
        runningman2left = new ImageIcon("images//left_2 (2).png").getImage();
        runningman3left = new ImageIcon("images//left_2 (1).png").getImage();
        runningman4left = new ImageIcon("images//left_2 (3).png").getImage();
        runningman2right = new ImageIcon("images//right_2 (2).png").getImage();
        runningman3right = new ImageIcon("images//right_2 (1).png").getImage();
        runningman4right = new ImageIcon("images//right_2 (3).png").getImage();
        bg = new ImageIcon("images//bg_2.png").getImage();
    }
    
    private void loadSounds() {
        try {
            soundtrack = Applet.newAudioClip(new URL("file:images//soundtrack7.wav"));
        } catch (MalformedURLException ex) {
            Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            lost = Applet.newAudioClip(new URL("file:images//gameover.wav"));
        } catch (MalformedURLException ex) {
            Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            win = Applet.newAudioClip(new URL("file:images//win.wav"));
        } catch (MalformedURLException ex) {
            Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            death = Applet.newAudioClip(new URL("file:images//jump_00.wav"));
        } catch (MalformedURLException ex) {
            Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            ding = Applet.newAudioClip(new URL("file:images//ding.wav"));
        } catch (MalformedURLException ex) {
            Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bg, 0, 0, this);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        Graphics2D g2d = (Graphics2D) g; 
        g2d.setColor(Color.black);

        drawMaze(g2d);
        drawScore(g2d);
        doAnim();

        if (ingame) {
            playGame(g2d);
        } 
        else {
            soundtrack.stop();
            showIntroScreen(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (ingame) {
                if (key == KeyEvent.VK_LEFT) {
                    reqdx = -1;
                    reqdy = 0;
                } 
                else if (key == KeyEvent.VK_RIGHT) {
                    reqdx = 1;
                    reqdy = 0;
                } 
                else if (key == KeyEvent.VK_UP) {
                    reqdx = 0;
                    reqdy = -1;
                } 
                else if (key == KeyEvent.VK_DOWN) {
                    reqdx = 0;
                    reqdy = 1;
                } 
                else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    ingame = false;
                } 
                else if (key == KeyEvent.VK_PAUSE) {
                    if (timer.isRunning()) {
                        timer.stop();
                    } 
                    else {
                        timer.start();
                    }
                }
            } 
            else {
                if (key == 's' || key == 'S') {
                    ingame = true;
                    soundtrack.loop();
                    initGame();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == Event.LEFT || key == Event.RIGHT
                    || key == Event.UP || key == Event.DOWN) {
                reqdx = 0;
                reqdy = 0;
            }
        }
    }
    
//    public static short[] getRandom(short[] array) {
//        short arr[] = new short[6];
//        int rnd;
//            for (int i =0; i < 6; i++){            
//                rnd = new Random().nextInt(array.length);       
//                arr[i] = array[rnd];
//            }
//        return arr;
//    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
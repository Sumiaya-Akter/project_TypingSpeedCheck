/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.project001;

import java.awt.Color;
import java.awt.Component;
import java.awt.List;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author SIAM
 */
public class TypingPage extends javax.swing.JFrame {

    /**
     * Creates new form TypingPage
     */
    int DurationInSecond;
    JFrame initial;

    public TypingPage() {
        initComponents();
    }

    public TypingPage(int duration, int wordLength, Component cmp) {
        initComponents();
        System.out.println(duration);
        DurationInSecond = duration * 60;
        loadText("words-len" + wordLength + ".txt");
        initial = (JFrame)cmp;
        startFunctionality();
    }
    Boolean paused, started = false;
    ArrayList<String> textsFromFile, selected500ForTyping;
    private String strSource, strTarget;
    private DefaultStyledDocument doc;
    private StyleContext styleContext;
    private Style blue, red, gray, normal;
    private int sourceCurrentWordStartIdx, targetCurrentWordStartIdx, offset, sourcePreviousWordStartIdx;
    private int wordCount = 0, charCount = 0;
    private KeyAdapter kAdapter;
    private java.util.List<Boolean> wordCorrect;
    private int wordCorrectIdx, elapsedTime;
    private char removedChar;
    CountDown CLOCK;
    private void loadText(String file) {
        textsFromFile = new ArrayList<>();
        try {
            BufferedReader bfr = new BufferedReader(new FileReader(file));
            String word = bfr.readLine();
            while (word != null) {
                textsFromFile.add(word);
                word = bfr.readLine();
            }
            bfr.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

       for (int i = 0; i < textsFromFile.size(); i++) {
            System.out.println(textsFromFile.get(i));
        }
    }
    
    private void startFunctionality() {

        ShuffleAndSelectWords();

        styleContext = new StyleContext();
        doc = new DefaultStyledDocument(styleContext);
        blue = styleContext.addStyle("BLUE", null);
        blue.addAttribute(StyleConstants.Foreground, Color.BLUE);
        gray = styleContext.addStyle("GRAY", null);
        gray.addAttribute(StyleConstants.Foreground, Color.GRAY);
        red = styleContext.addStyle("RED", null);
        red.addAttribute(StyleConstants.Foreground, Color.RED);
        normal = styleContext.addStyle("NORMAL", null);
        normal.addAttribute(StyleConstants.Foreground, Color.BLACK);

        applyInitialDesign();

        sourceTextPane.setEditable(false);
        sourceTextPane.setCaretPosition(100);
        strSource = sourceTextPane.getText();

        
        wordCorrect = new ArrayList<>();
        wordCorrect.add(false);
        wordCorrectIdx = 0;

        kAdapter = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!started) {
                    sourcePreviousWordStartIdx = 0;
                    sourceCurrentWordStartIdx = 0;
                    targetCurrentWordStartIdx = 0;
                    CLOCK = new CountDown(TypingPage.this, DurationInSecond);
                    started = true; 
                } else if (offset > 0) {
                    int sz = strTarget.length();
                    removedChar = strTarget.charAt(sz - 1);
                }
            }

            public void keyReleased(KeyEvent e) {
                strTarget = targetTextPane.getText();
                offset = targetTextPane.getCaretPosition() - 1;
                if (e.getKeyChar() == ' ')
                    applySpacePressedStyle(offset);
                else if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE)
                    applyBackspacePressedStyle(offset);
                else
                    applyNewCharacterTypedStyle(offset);
            }
        };
        targetTextPane.addKeyListener(kAdapter);
    }

    private void ShuffleAndSelectWords() {

        int txt_size = textsFromFile.size();
        int final_size = txt_size;

        Random random = new Random();
        selected500ForTyping = new ArrayList<>();
        for (int i = 0; i < final_size; i++) {
            int idx = random.nextInt(txt_size);
            selected500ForTyping.add(textsFromFile.remove(idx));
            txt_size = textsFromFile.size();
        }
    }

    private void applyInitialDesign() {

        sourceTextPane.setDocument(doc);
        try {
            doc.insertString(0, selected500ForTyping.get(0) + " ", blue);
            for (int i = 1; i < selected500ForTyping.size(); i++) {
                String string = selected500ForTyping.get(i);
                int start = sourceTextPane.getText().length();
                doc.insertString(start, string + " ", normal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private void applyNewCharacterTypedStyle(int offset) {

        int near_space;
        Style style;

        if (offset < 0)
            return;

        near_space = strSource.indexOf(' ', sourceCurrentWordStartIdx);

        style = blue;
        for (int i = targetCurrentWordStartIdx, j = 0; i <= offset; i++, j++) {
            if (j >= near_space) {
                style = red;
                break;
            }
            if (strSource.charAt(sourceCurrentWordStartIdx + j) != strTarget.charAt(i)) {
                style = red; 
                break;
            }
        }

        doc.setCharacterAttributes(sourceCurrentWordStartIdx, near_space - sourceCurrentWordStartIdx, style, true);
    }

    private void applySpacePressedStyle(int offset) { 

        sourceTextPane.setCaretPosition(Integer.min(sourceCurrentWordStartIdx + 100, strSource.length()));

        int near_space, end;
        Style style;
        String s1, s, s2;
//        StringBuilder sBuilder = new StringBuilder(); abc 
//
//        sBuilder.append(strTarget);
//        sBuilder.reverse();
//        s = sBuilder.toString();
//        end = s.indexOf(' ', 1);
//        if (end == -1)
//            end = s.length();
//        s2 = s.substring(1, Integer.min(s.length(), end));
//        sBuilder.delete(0, sBuilder.length());
//        sBuilder.append(s2);
//        sBuilder.reverse();
//        s2 = sBuilder.toString();
        near_space = strTarget.indexOf(' ', targetCurrentWordStartIdx);
        s2 = strTarget.substring(targetCurrentWordStartIdx, near_space);

        near_space = strSource.indexOf(' ', sourceCurrentWordStartIdx);
        s1 = strSource.substring(sourceCurrentWordStartIdx, near_space);

        if (s1.equals(s2)) {
            style = gray;

            wordCorrect.set(wordCorrectIdx, true);
            wordCount += 1;
            charCount += near_space - sourceCurrentWordStartIdx;
        } else
            style = red;
        doc.setCharacterAttributes(sourceCurrentWordStartIdx, near_space - sourceCurrentWordStartIdx, style, true);

        sourcePreviousWordStartIdx = sourceCurrentWordStartIdx;
        sourceCurrentWordStartIdx = near_space + 1;
        targetCurrentWordStartIdx = strTarget.length();

        int next_space = strSource.indexOf(' ', sourceCurrentWordStartIdx); 
        if (next_space != -1)
            doc.setCharacterAttributes(sourceCurrentWordStartIdx, next_space - sourceCurrentWordStartIdx, blue, true);
        else
            CLOCK.forceStop();

        wordCorrectIdx++;
        wordCorrect.add(false);
        updateTypingSpeed();
    }

    private void applyBackspacePressedStyle(int offset) {

        if (removedChar == ' ') {
            int near_space;
            near_space = strSource.indexOf(' ', sourceCurrentWordStartIdx);
            doc.setCharacterAttributes(sourceCurrentWordStartIdx, near_space - sourceCurrentWordStartIdx, normal, true);
            sourceCurrentWordStartIdx = sourcePreviousWordStartIdx;
            near_space = strSource.indexOf(' ', sourceCurrentWordStartIdx);
            doc.setCharacterAttributes(sourceCurrentWordStartIdx, near_space - sourceCurrentWordStartIdx, blue, true);
            sourcePreviousWordStartIdx = prevIndex(strSource, ' ', sourcePreviousWordStartIdx - 2);

            if (wordCorrect.get(--wordCorrectIdx)) {
                wordCount -= 1;
                charCount -= near_space - sourceCurrentWordStartIdx;
                updateTypingSpeed();
            }
        } else {
            applyNewCharacterTypedStyle(offset); 
        }
    }

    private int prevIndex(String string, char c, int from) {

        int pos = 0;
        for (int i = from; i >= 0; i--) {
            if (string.charAt(i) == c) {
                pos = i + 1;
                break;
            }
        }
        return pos;
    }
    
    private void updateTypingSpeed() {

        cpmLabel.setText(Integer.toString(charCount));
        wpmLabel.setText(Integer.toString(wordCount));
    }

    public void UpdateTimeInUI(int elapsed, int remaining) {
        elapsedTime = elapsed;
        LocalDateTime elp, rem;
        DateTimeFormatter ofFormat = DateTimeFormatter.ofPattern("mm:ss");
        elp = LocalDateTime.of(2020, 1, 1, 1, elapsed/60, elapsed%60);
        rem = LocalDateTime.of(2020, 1, 1, 1, remaining/60, remaining%60);
        elapsedTimeLabel.setText(elp.format(ofFormat));
        remainingTimeLabel.setText(rem.format(ofFormat));
    }
    public void CountDownFinished() {

        targetTextPane.setEditable(false);
        targetTextPane.removeKeyListener(kAdapter);

        saveResult();
    }

    private void saveResult() {
        String wp, cp, msg;
        try {
            BufferedWriter bfw = new BufferedWriter(new FileWriter("result.txt"));
            wp = Integer.toString(wordCount*60/elapsedTime);
            cp = Integer.toString(charCount*60/elapsedTime);
            bfw.write(wp + "\n" + cp);
            bfw.close();
            msg = "Your result is " + wp;
            msg += " words per minute\n";
            msg += "And " + cp + " characters per minute.\n";
            JOptionPane.showOptionDialog(this, msg, "Result", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null);
        } catch (Exception e) {
            // e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        wpmLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        remainingTimeLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        elapsedTimeLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cpmLabel = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourceTextPane = new javax.swing.JTextPane();
        jLabel9 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        targetTextPane = new javax.swing.JTextPane();
        jPanel4 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        wpmLabel.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        wpmLabel.setText("0");

        jLabel2.setText("words");

        remainingTimeLabel.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        remainingTimeLabel.setText("00:00");

        jLabel4.setText("reaminging");

        elapsedTimeLabel.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        elapsedTimeLabel.setText("00:00");

        jLabel6.setText("elapsed");

        cpmLabel.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        cpmLabel.setText("0");

        jLabel8.setText("characters");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(remainingTimeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(44, 44, 44)
                .addComponent(wpmLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(68, 68, 68)
                .addComponent(cpmLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(elapsedTimeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addGap(21, 21, 21))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wpmLabel)
                    .addComponent(remainingTimeLabel)
                    .addComponent(elapsedTimeLabel)
                    .addComponent(cpmLabel))
                .addGap(27, 27, 27))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel4))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel8)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sourceTextPane.setEditable(false);
        sourceTextPane.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        sourceTextPane.setText("Here is some of sample texts...");
        jScrollPane1.setViewportView(sourceTextPane);

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel9.setText("Text to Type:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel10.setText("Type below text here:");

        jScrollPane2.setViewportView(targetTextPane);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addContainerGap())
        );

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton1.setText("Restart");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addGap(19, 19, 19))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(13, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        this.dispose();
        initial.dispose();
        new InitialPage().setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TypingPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TypingPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TypingPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TypingPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TypingPage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cpmLabel;
    private javax.swing.JLabel elapsedTimeLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel remainingTimeLabel;
    private javax.swing.JTextPane sourceTextPane;
    private javax.swing.JTextPane targetTextPane;
    private javax.swing.JLabel wpmLabel;
    // End of variables declaration//GEN-END:variables
}

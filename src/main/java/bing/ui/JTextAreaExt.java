package bing.ui;

import bing.Constants;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import org.apache.commons.lang3.StringUtils;

/**
 * 带右键菜单的JTextArea
 *
 * @author IceWee
 */
public class JTextAreaExt extends JTextArea implements MouseListener {

    private JPopupMenu popMenu = null; // 弹出菜单
    private JMenuItem menuItemCopy = null, menuItemCut = null, menuItemClear = null; // 功能菜单
    static final ImageIcon ICON_COPY = new ImageIcon(JTextAreaExt.class.getResource(Constants.ICON_COPY_PATH));
    static final ImageIcon ICON_CUT = new ImageIcon(JTextAreaExt.class.getResource(Constants.ICON_CUT_PATH));
    static final ImageIcon ICON_CLEAR = new ImageIcon(JTextAreaExt.class.getResource(Constants.ICON_CLEAR_PATH));

    public JTextAreaExt() {
        super();
        init();
    }

    private void init() {
        this.addMouseListener(this);
        popMenu = new JPopupMenu();
        menuItemCopy = new JMenuItem(Constants.TEXT_COPY);
        menuItemCopy.setIcon(ICON_COPY);
        menuItemCut = new JMenuItem(Constants.TEXT_CUT);
        menuItemCut.setIcon(ICON_CUT);
        menuItemClear = new JMenuItem(Constants.TEXT_CLEAR);
        menuItemClear.setIcon(ICON_CLEAR);
        popMenu.add(createSeparator());
        popMenu.add(menuItemCopy);
        popMenu.add(createSeparator());
        popMenu.add(menuItemCut);
        popMenu.add(createSeparator());
        popMenu.add(menuItemClear);
        menuItemCopy.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK));
        menuItemCut.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_MASK));
        menuItemCopy.addActionListener((ActionEvent e) -> {
            action(e);
        });
        menuItemCut.addActionListener((ActionEvent e) -> {
            action(e);
        });
        menuItemClear.addActionListener((ActionEvent e) -> {
            action(e);
        });
        this.add(popMenu);
    }

    /**
     * 菜单动作
     *
     * @param e
     */
    public void action(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals(menuItemCopy.getText())) { // 复制
            this.copy();
        } else if (cmd.equals(menuItemCut.getText())) { // 剪切
            this.cut();
        } else if (cmd.equals(menuItemClear.getText())) { // 清除
            this.setText(StringUtils.EMPTY);
        }
    }

    /**
     * 文本区域是否有内容
     *
     * @return
     */
    private boolean allowClear() {
        return StringUtils.isNotBlank(this.getText());
    }

    /**
     * 文本组件中是否具备复制的条件
     *
     * @return
     */
    private boolean allowCopy() {
        return this.getSelectionStart() != this.getSelectionEnd();
    }

    private JSeparator createSeparator() {
        return new JSeparator();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            menuItemCopy.setEnabled(allowCopy());
            menuItemCut.setEnabled(allowCopy());
            menuItemClear.setEnabled(allowClear());
            popMenu.show(this, e.getX(), e.getY());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

}

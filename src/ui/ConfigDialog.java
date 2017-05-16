package ui;

import business.ConvertBridge;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiFile;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import model.ConfigData;
import model.KeyTypeItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConfigDialog extends JDialog {
    private JPanel mContentPane;
    private JButton mButtonOK;
    private JButton mButtonCancel;
    private JTextField mFirstDomainTxt;
    private JTextField mSecondDomainTxt;
    private JTextField mUrlPathTxt;
    private JButton mGetParamPlusBtn;
    private JButton mPostParamPlusBtn;
    private JEditorPane mRespJsonSpan;
    private JCheckBox mPlainCheckBox;
    private JButton mCheckJsonBtn;
    private JPanel mPostSpan;
    private JPanel mGetSpan;
    private PsiFile mFile;
    private ConfigData mConfigData = new ConfigData();

    private ActionListener mDeleteListener = e -> {
        if (e.getSource() instanceof JButton) {
            JButton btn = (JButton) e.getSource();
            Container container;
            if ("get".equals(e.getActionCommand())) {
                container = mGetSpan;
            } else {
                container = mPostSpan;
            }
            container.remove(btn.getParent());
            container.revalidate();
            container.repaint();
        }
    };

    public ConfigDialog(PsiFile file) {
        mFile = file;
        setSize(1000, 600);
        setPreferredSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setContentPane(mContentPane);
        setModal(true);
        getRootPane().setDefaultButton(mButtonOK);

        mButtonOK.addActionListener(e -> onOK());

        mButtonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        mContentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        initGetParamList();
        initPostParamList();
    }

    private void initGetParamList() {
        mGetSpan.setLayout(new BoxLayout(mGetSpan, BoxLayout.Y_AXIS));
        mGetSpan.add(Box.createVerticalGlue());
        mGetParamPlusBtn.addActionListener(actionEvent -> {
            addColumn(mGetSpan);
            mGetSpan.revalidate();
            mGetSpan.repaint();
        });
    }

    private void initPostParamList() {
        mPostSpan.setLayout(new BoxLayout(mPostSpan, BoxLayout.Y_AXIS));
        mPostSpan.add(Box.createVerticalGlue());
        mPostParamPlusBtn.addActionListener(e -> {
            addColumn(mPostSpan);
            mPostSpan.revalidate();
            mPostSpan.repaint();
        });
    }

    private void addColumn(Container container) {
        JPanel panel = new JPanel(new GridLayoutManager(1, 3));
        panel.setMaximumSize(new Dimension(1000, 50));
        JTextField keyTxt = new JTextField();
        ComboBox valueBox = new ComboBox<>(KeyTypeItem.TYPE_VALUES);
        valueBox.setSelectedIndex(0);
        JButton deleteBtn = new JButton("-");
        deleteBtn.setActionCommand(container.equals(mGetSpan) ? "get" : "post");
        deleteBtn.addActionListener(mDeleteListener);
        panel.add(keyTxt, new GridConstraints(0, 0, 1, 1, 0, 1, 6, 1, null, null, null));
        panel.add(valueBox, new GridConstraints(0, 1, 1, 1, 0, 1, 1, 1, null, null, null));
        panel.add(deleteBtn, new GridConstraints(0, 2, 1, 1, 0, 1, 1, 1, null, null, null));
        container.add(panel, container.getComponentCount() - 1);
    }

    private void onOK() {
        collectData();
        System.out.println(mConfigData);
        new ConvertBridge(mConfigData, mFile).run();
        // add your code here
        dispose();
    }

    private void collectData() {
        mConfigData.firstDomain = mFirstDomainTxt.getText();
        mConfigData.secondDomain = mSecondDomainTxt.getText();
        mConfigData.urlPath = mUrlPathTxt.getText();

        collectGetParams();
        collectPostParams();

        mConfigData.isPlain = mPlainCheckBox.isSelected();
        mConfigData.respJson = mRespJsonSpan.getText();
    }

    private void collectGetParams() {
        for (int i = 0; i < mGetSpan.getComponentCount(); i++) {
            Component component = mGetSpan.getComponent(i);
            if (component instanceof JPanel) {
                String fieldName = null;
                int index = -1;
                for (int j = 0; j < ((JPanel) component).getComponentCount(); j++) {
                    Component child = ((JPanel) component).getComponent(j);
                    if (child instanceof JTextField) {
                        fieldName = ((JTextField) child).getText();
                    } else if (child instanceof ComboBox) {
                        index = ((ComboBox) child).getSelectedIndex();
                    }
                }
                if (fieldName != null && fieldName.trim().length() > 0 && index != -1) {
                    mConfigData.addGetParam(fieldName, index);
                }
            }
        }
    }

    private void collectPostParams() {
        for (int i = 0; i < mPostSpan.getComponentCount(); i++) {
            Component component = mPostSpan.getComponent(i);
            if (component instanceof JPanel) {
                String fieldName = null;
                int index = -1;
                for (int j = 0; j < ((JPanel) component).getComponentCount(); j++) {
                    Component child = ((JPanel) component).getComponent(j);
                    if (child instanceof JTextField) {
                        fieldName = ((JTextField) child).getText();
                    } else if (child instanceof ComboBox) {
                        index = ((ComboBox) child).getSelectedIndex();
                    }
                }
                if (fieldName != null && fieldName.trim().length() > 0 && index != -1) {
                    mConfigData.addPostParam(fieldName, index);
                }
            }
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        ConfigDialog dialog = new ConfigDialog(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}

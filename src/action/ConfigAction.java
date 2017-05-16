package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiFile;
import ui.ConfigDialog;

/**
 * 打开主弹窗
 * Created by assistne on 2017/5/10.
 */
public class ConfigAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);
        // 打开弹窗
        ConfigDialog dialog = new ConfigDialog(file);
        dialog.setVisible(true);
    }
}

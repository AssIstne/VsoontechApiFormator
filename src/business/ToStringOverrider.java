package business;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

/**
 * 负责重写Java类的toString方法
 * Created by assistne on 2017/5/16.
 */
public class ToStringOverrider {
    public void overrideToString(PsiClass psiClass) {
        if (psiClass != null && psiClass.getAllFields().length > 0) {
            PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(psiClass.getProject());
            PsiMethod method = elementFactory.createMethodFromText("@Override\npublic String toString() {}", psiClass);
            StringBuilder res = new StringBuilder("\"" + psiClass.getName() + "{\" + \n");
            for (PsiField field : psiClass.getAllFields()) {
                res.append("\"").append(field.getName()).append("=\"").append(" + ").append(field.getName()).append(" + \n");
            }
            res.append("\"}\"");
            method.getBody().add(elementFactory.createStatementFromText("return "+ res +";", method));
            if (psiClass.getMethods().length > 0) {
                psiClass.addAfter(method, psiClass.getMethods()[psiClass.getMethods().length - 1]);
            } else if (psiClass.getInnerClasses().length > 0) {
                psiClass.addBefore(method, psiClass.getInnerClasses()[0]);
            } else {
                psiClass.addAfter(method, psiClass.getFields()[psiClass.getFields().length - 1]);
            }
        }
    }
}
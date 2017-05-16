package business;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import model.ConfigData;
import model.KeyTypeItem;

import java.util.List;

/**
 * Created by assistne on 2017/5/16.
 */
public class ConvertBridge {
    private ConfigData mConfigData;
    private PsiFile mFile;
    private ToStringOverrider mOverrider = new ToStringOverrider();
    private JsonToFieldUtil mJsonUtil = new JsonToFieldUtil();

    public ConvertBridge(ConfigData configData, PsiFile file) {
        this.mConfigData = configData;
        this.mFile = file;
    }

    public void run() {
        try {
            WriteCommandAction.runWriteCommandAction(mFile.getProject(), () -> {
                createRespFile();
                modifyReqFile();
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void modifyReqFile() {
        PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(mFile.getProject());
        PsiJavaFile javaFile = (PsiJavaFile) mFile;
        PsiClass mainClass = javaFile.getClasses()[0];
        PsiClass extendClass;
        if (mConfigData.postParams.isEmpty()) {
            extendClass = getGetRequestClass();
        } else {
            // post
            extendClass = getPostRequestClass();
        }
        if (extendClass == null) {
            // 没有引入类
        } else {
            // extend
            for (PsiElement element : mainClass.getExtendsList().getReferenceElements()) {
                element.delete();
            }
            // 添加继承
            mainClass.getExtendsList().add(elementFactory.createClassReferenceElement(extendClass));
            PsiMethod[] existedMethod;
            PsiMethod psiMethod;
            String returnValue;
            returnValue = mConfigData.firstDomain != null ? mConfigData.firstDomain.trim() : null;
            existedMethod = mainClass.findMethodsByName("getDomainName", false);
            for (PsiMethod method : existedMethod) {
                method.delete();
            }
            // 重写getDomainName
            if (returnValue != null && returnValue.length() > 0) {
                psiMethod = elementFactory.createMethodFromText(
                        "@Override\n" +
                                "protected String getDomainName() {\n" +
                                "return \"" + returnValue + "\";\n}",
                        null);
                mainClass.add(psiMethod);
            }
            // 重写getSecondDomainName
            existedMethod = mainClass.findMethodsByName("getSecondDomainName", false);
            for (PsiMethod method : existedMethod) {
                method.delete();
            }
            returnValue = mConfigData.secondDomain == null ? null : mConfigData.secondDomain.trim();
            psiMethod = elementFactory.createMethodFromText(
                    "@Override\n" +
                            "protected String getSecondDomainName() {\n" +
                            "return \"" + returnValue + "\";\n}",
                    null);
            mainClass.add(psiMethod);
            // 重写getApi
            existedMethod = mainClass.findMethodsByName("getApi", false);
            for (PsiMethod method : existedMethod) {
                method.delete();
            }
            returnValue = mConfigData.urlPath == null ? null : mConfigData.urlPath.trim();
            psiMethod = elementFactory.createMethodFromText(
                    "@Override\n" +
                            "protected String getApi() {\n" +
                            "return \"" + returnValue + "\";\n}",
                    null);

            mainClass.add(psiMethod);
            existedMethod = mainClass.findMethodsByName("reqType", false);
            for (PsiMethod method : existedMethod) {
                method.delete();
            }
            // 修改为明文请求
            if (mConfigData.isPlain) {
                mainClass.addBefore(elementFactory.createImportStatement(
                        JavaPsiFacade.getInstance(mFile.getProject())
                                .findClass("com.linkin.base.nhttp.base.ReqType", GlobalSearchScope.allScope(mFile.getProject()))), mainClass);
                psiMethod = elementFactory.createMethodFromText(
                    "@Override\n" +
                            "public int reqType() {\n" +
                            "return ReqType.PLAIN;\n}",
                    null
                );
                mainClass.add(psiMethod);
            }
            PsiClass getParamClass = mainClass.findInnerClassByName("Param", true);
            if (getParamClass != null) {
                getParamClass.delete();
                getParamClass = null;
            }
            if (!mConfigData.getParams.isEmpty()) {
                // 创建内部类表示get请求参数
                getParamClass = createAndAddFieldAndMethodToParamClass("Param", mConfigData.getParams);
                mainClass.add(getParamClass);
            }
            PsiClass postParamClass = mainClass.findInnerClassByName("PostParam", true);
            if (postParamClass != null) {
                postParamClass.delete();
                postParamClass = null;
            }

            if (!mConfigData.postParams.isEmpty()) {
                // 创建内部类表示post请求参数
                postParamClass = createAndAddFieldAndMethodToParamClass("PostParam", mConfigData.postParams);
                mainClass.add(postParamClass);
            }
            for (PsiMethod method : mainClass.findMethodsByName(mainClass.getName(), false)) {
                method.delete();
            }
            if (getParamClass != null || postParamClass != null) {// 根据get/post参数创建构造函数
                PsiMethod constructorMethod = elementFactory.createConstructor(mainClass.getName());
                PsiCodeBlock body = constructorMethod.getBody();
                PsiMethod c;
                // get
                // 获取get参数内部类的构造函数
                if (getParamClass != null && getParamClass.findMethodsByName(getParamClass.getName(), false).length > 0) {
                    c = getParamClass.findMethodsByName(getParamClass.getName(), false)[0];
                    StringBuilder paramListString = new StringBuilder();
                    for (PsiParameter parameter : c.getParameterList().getParameters()) {
                        if (c.getParameterList().getParameterIndex(parameter) != 0) {
                            paramListString.append(", ");
                        }
                        // 复制到类的构造函数
                        constructorMethod.getParameterList().add(parameter);
                        paramListString.append(parameter.getName());
                    }
                    body.add(elementFactory.createStatementFromText(
                            "setParamObject(new " + getParamClass.getName() + "(" + paramListString + "));", constructorMethod));
                }
                // post
                if (postParamClass != null && postParamClass.findMethodsByName(postParamClass.getName(), false).length > 0) {
                    c = postParamClass.findMethodsByName(postParamClass.getName(), false)[0];
                    StringBuilder paramListString = new StringBuilder();
                    for (PsiParameter parameter : c.getParameterList().getParameters()) {
                        if (c.getParameterList().getParameterIndex(parameter) != 0) {
                            paramListString.append(", ");
                        }    // 复制到类的构造函数
                        constructorMethod.getParameterList().add(parameter);
                        paramListString.append(parameter.getName());
                    }
                    body.add(elementFactory.createStatementFromText(
                            "setPostObject(new " + postParamClass.getName() + "(" + paramListString + "));", constructorMethod));
                }
                mainClass.addBefore(constructorMethod, mainClass.getMethods()[0]);
            }
        }
    }

    private PsiClass getGetRequestClass() {
        return JavaPsiFacade.getInstance(mFile.getProject())
                .findClass("com.linkin.base.nhttp.base.GetRequest", GlobalSearchScope.allScope(mFile.getProject()));
    }

    private PsiClass getPostRequestClass() {
        return JavaPsiFacade.getInstance(mFile.getProject())
                .findClass("com.linkin.base.nhttp.base.PostRequest", GlobalSearchScope.allScope(mFile.getProject()));
    }

    private PsiClass createAndAddFieldAndMethodToParamClass(String className, List<KeyTypeItem> params) {
        PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(mFile.getProject());
        PsiClass paramClass = elementFactory.createClass(className);
        // 把默认的public改为private
        paramClass.getModifierList().getFirstChild().delete();
        paramClass.getModifierList().add(elementFactory.createKeyword("private"));
        // 改为静态类
        paramClass.getModifierList().add(elementFactory.createKeyword("static"));
        // 构建函数
        PsiMethod constructorMethod = elementFactory.createConstructor(className);
        PsiField psiField;
        PsiType psiFieldType;
        for (KeyTypeItem item : params) {
            String key = item.key;
            int typeIndex = item.typeIndex;
            if (typeIndex == 0) {
                psiFieldType = elementFactory.createType(JavaPsiFacade.getInstance(mFile.getProject())
                        .findClass("java.lang.String", GlobalSearchScope.allScope(mFile.getProject())));

            } else {// 基本类型
                psiFieldType = elementFactory.createPrimitiveTypeFromText(KeyTypeItem.TYPE_VALUES[typeIndex]);
            }
            psiField = elementFactory.createField(key, psiFieldType);
            paramClass.add(psiField);
            constructorMethod.getParameterList().add(elementFactory.createParameter(item.key, psiFieldType));

            constructorMethod.getBody().add(elementFactory.createStatementFromText("this." + item.key + "=" + item.key + ";", constructorMethod));
        }
        paramClass.add(constructorMethod);
        mOverrider.overrideToString(paramClass);
        return paramClass;
    }

    // 创建响应文件
    private void createRespFile() {
        if (mConfigData.respJson != null && mConfigData.respJson.trim().length() > 0) {
            PsiJavaFile psiJavaFile = (PsiJavaFile)mFile;
            PsiClass mainClass = psiJavaFile.getClasses()[0];
            String currentFileName = mainClass.getName();
            String respFileName = getRespFileName(currentFileName);
            try {
                PsiClass respClass = null;
                for (PsiFile f : mFile.getParent().getFiles()) {
                    if (f.getName().equals(respFileName + ".java")) {
                        respClass = ((PsiJavaFile)f).getClasses()[0];
                    }
                }
                if (respClass == null) {
                    respClass = JavaDirectoryService.getInstance().createClass(mFile.getParent(), respFileName);
                }
                // 根据输入的Json创建类成员
                mJsonUtil.run(respClass, mConfigData.respJson);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 根据当前文件的名字获取返回文件的文件名
    private String getRespFileName(String currentFileName) {
        if (currentFileName.contains("Req")) {
            return currentFileName.replaceFirst("Req", "Resp");
        } else {
            return currentFileName + "Resp";
        }
    }
}

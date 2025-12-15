package com.aistd.ikraiaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aistd.ikraiaicodemother.exception.BusinessException;
import com.aistd.ikraiaicodemother.exception.ErrorCode;
import com.aistd.ikraiaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码保存器 - 模板方法模式
 *
 * @author ikrai
 */
public abstract class CodeFileSaverTemplate<T> {
    //文件根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 模板方法：保存代码的标准流程
     *
     * @param result 代码结果对象
     * @return 保存的目录
     */
    public final File saveCode(T result){
        // 1. 验证输入
        validateInput(result);
        // 2. 构建唯一目录
        String baseDirPath = buildUniqueDir();
        // 3. 保存文件（具体实现由子类提供）
        saveFiles(result,baseDirPath);
        // 4. 返回目录文件对象
        return new File(baseDirPath);
    }

    /**
     * 验证输入参数是否有效(可由子类覆盖)
     * @param result 代码结果对象
     */
    protected void validateInput(T result) {
        // 验证结果是否为空
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"生成的代码结果不能为空");
        }
    }

    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID
     */
    protected final String buildUniqueDir() {
        String codeType=getCodeType().getValue();
        String uniqueDirName= StrUtil.format("{}_{}",codeType, IdUtil.getSnowflakeNextIdStr());
        String dirPath= FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件的工具方法
     */
    protected static void writeToFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)){
            String filePath=dirPath+ File.separator +filename;
            FileUtil.writeString(content,filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取代码类型(由子类实现)
     * @return 代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeType();
    /**
     * 保存文件的具体实现(由子类实现)
     * @param result 代码结果对象
     * @param baseDirPath 基础目录路径
     */
    protected abstract void saveFiles(T result, String baseDirPath);
}

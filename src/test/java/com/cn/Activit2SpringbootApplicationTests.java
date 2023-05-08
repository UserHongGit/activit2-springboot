package com.cn;

import com.cn.utils.SecurityUtil;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class Activit2SpringbootApplicationTests {


    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private RepositoryService repositoryService;

    @Test
    void contextLoads() {
        System.out.println(taskRuntime);
    }

    /**
     * 查询流程的定义
     */
    @Test
    public void test02(){
        securityUtil.logInAs("system");
        Page<ProcessDefinition> processDefinitionPage =
                processRuntime.processDefinitions(Pageable.of(0, 10));
        System.out.println("可用的流程定义数量:" + processDefinitionPage.getTotalItems());
        for (ProcessDefinition processDefinition : processDefinitionPage.getContent()) {
            System.out.println("流程定义：" + processDefinition);
        }
    }

    /**
     * 部署流程
     */
    @Test
    public void test03(){
        repositoryService.createDeployment()
                .addClasspathResource("process/my-evection.bpmn20.xml")
                .addClasspathResource("process/my-evection.png")
                .name("SpringBoot出差申请单")
                .deploy();
    }

    /**
     * 启动流程实例
     */
    @Test
    public void test04(){
        securityUtil.logInAs("system");
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("my-evection")
                .build()
        );
        System.out.println("流程实例id:" + processInstance.getId());
    }

    @Test
    public void test06(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        // 获取一个 ProcessDefinitionQuery对象 用来查询操作
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<org.activiti.engine.repository.ProcessDefinition> list = processDefinitionQuery.processDefinitionKey("my-evection")
                .orderByProcessDefinitionVersion() // 安装版本排序
                .desc() // 倒序
                .list();
        // 输出流程定义的信息
        for (org.activiti.engine.repository.ProcessDefinition processDefinition : list) {
            System.out.println("流程定义的ID：" + processDefinition.getId());
            System.out.println("流程定义的name：" + processDefinition.getName());
            System.out.println("流程定义的key:" + processDefinition.getKey());
            System.out.println("流程定义的version:" + processDefinition.getVersion());
            System.out.println("流程部署的id:" + processDefinition.getDeploymentId());
        }

    }
    /**
     * 任务查询、拾取及完成操作
     */
    @Test
    public void test05(){
        securityUtil.logInAs("jack");
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10));
        if(tasks != null && tasks.getTotalItems() > 0){
            for (Task task : tasks.getContent()) {
                // 拾取任务
                taskRuntime.claim(TaskPayloadBuilder
                        .claim()
                        .withTaskId(task.getId())
                        .build()
                );
                System.out.println("任务：" + task);
                taskRuntime.complete(TaskPayloadBuilder
                        .complete()
                        .withTaskId(task.getId())
                        .build()
                );
            }
        }
        Page<Task> taskPage2 = taskRuntime.tasks(Pageable.of(0,10));
        if(taskPage2 .getTotalItems() > 0){
            System.out.println("任务：" + taskPage2.getContent());
        }
    }




}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.dolphinscheduler.e2e.pages.project.workflow;

import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.HttpTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.JavaTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.PythonTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.ShellTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SubWorkflowTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SwitchTaskForm;

import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.Getter;
import lombok.SneakyThrows;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.google.common.io.Resources;

@SuppressWarnings("UnstableApiUsage")
@Getter
public final class WorkflowForm {

    private WebDriver driver;
    private final WorkflowSaveDialog saveForm;
    private final WorkflowFormatDialog formatDialog;

    @FindBy(className = "graph-format")
    private WebElement formatBtn;

    @FindBy(className = "btn-save")
    private WebElement buttonSave;

    public WorkflowForm(WebDriver driver) {
        this.driver = driver;
        this.saveForm = new WorkflowSaveDialog(this);
        this.formatDialog = new WorkflowFormatDialog(this);

        PageFactory.initElements(driver, this);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T addTask(TaskType type) {
        final WebElement task = driver.findElement(By.className("task-item-" + type.name()));
        final WebElement canvas = driver.findElement(By.className("dag-container"));

        final JavascriptExecutor js = (JavascriptExecutor) driver;
        final String dragAndDrop = String.join("\n",
                Resources.readLines(Resources.getResource("dragAndDrop.js"), StandardCharsets.UTF_8));
        js.executeScript(dragAndDrop, task, canvas);
        WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions
                .visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Current node settings')]")));

        switch (type) {
            case SHELL:
                return (T) new ShellTaskForm(this);
            case SUB_WORKFLOW:
                return (T) new SubWorkflowTaskForm(this);
            case SWITCH:
                return (T) new SwitchTaskForm(this);
            case HTTP:
                return (T) new HttpTaskForm(this);
            case JAVA:
                return (T) new JavaTaskForm(this);
            case PYTHON:
                return (T) new PythonTaskForm(this);
        }
        throw new UnsupportedOperationException("Unknown task type");
    }

    public WebElement getTask(String taskName) {
        List<WebElement> tasks = WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.cssSelector("svg > g > g[class^='x6-graph-svg-stage'] > g[data-shape^='dag-task']")));

        WebElement task = tasks.stream()
                .filter(t -> t.getText().contains(taskName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such task: " + taskName));

        Actions action = new Actions(driver);
        action.doubleClick(task).build().perform();

        return task;
    }

    public WorkflowSaveDialog submit() {
        buttonSave().click();
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(.,'Basic Information')]")));
        return new WorkflowSaveDialog(this);
    }

    public WorkflowFormatDialog formatDAG() {
        formatBtn.click();

        return new WorkflowFormatDialog(this);
    }

    public enum TaskType {
        SHELL,
        SUB_WORKFLOW,
        SWITCH,
        HTTP,
        JAVA,
        PYTHON
    }
}

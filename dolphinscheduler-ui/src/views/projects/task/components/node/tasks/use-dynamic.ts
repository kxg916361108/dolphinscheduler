/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import * as Fields from '../fields/index'
import type { IJsonItem, INodeData } from '../types'
import { ITaskData } from '../types'

export function useDynamic({
  projectCode,
  from = 0,
  readonly,
  data
}: {
  projectCode: number
  from?: number
  readonly?: boolean
  data?: ITaskData
}) {
  const router = useRouter()
  const workflowCode = router.currentRoute.value.params.code
  const model = reactive({
    taskType: 'DYNAMIC',
    name: '',
    flag: 'YES',
    description: '',
    timeoutFlag: false,
    localParams: [],
    environmentCode: null,
    failRetryInterval: 1,
    failRetryTimes: 0,
    workerGroup: 'default',
    delayTime: 0,
    timeout: 30,
    maxNumOfSubWorkflowInstances: 1024,
    degreeOfParallelism: 1,
    filterCondition: '',
    listParameters: [{ name: null, value: null, separator: ',' }]
  } as INodeData)

  if (model.listParameters?.length) {
    model.listParameters[0].disabled = true
  }

  return {
    json: [
      Fields.useName(from),
      ...Fields.useTaskDefinition({ projectCode, from, readonly, data, model }),
      Fields.useRunFlag(),
      Fields.useDescription(),
      Fields.useTaskPriority(),
      Fields.useWorkerGroup(projectCode),
      Fields.useEnvironmentName(model, !data?.id),
      ...Fields.useTaskGroup(model, projectCode),
      ...Fields.useTimeoutAlarm(model),
      Fields.useChildNode({
        model,
        projectCode,
        from,
        workflowName: data?.workflowDefinitionName,
        code: from === 1 ? 0 : Number(workflowCode)
      }),
      ...Fields.useDynamic(model),
      Fields.usePreTasks()
    ] as IJsonItem[],
    model
  }
}

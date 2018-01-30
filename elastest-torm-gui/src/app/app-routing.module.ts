import { TestCaseComponent } from './etm-testlink/test-case/test-case.component';
import { TestCaseFormComponent } from './etm-testlink/test-case/test-case-form/test-case-form.component';
import { TestSuiteComponent } from './etm-testlink/test-suite/test-suite.component';
import { TestSuiteFormComponent } from './etm-testlink/test-suite/test-suite-form/test-suite-form.component';
import { TestPlanComponent } from './etm-testlink/test-plan/test-plan.component';
import { HelpComponent } from './elastest-etm/help/help.component';
import { ElastestLogAnalyzerComponent } from './elastest-log-analyzer/elastest-log-analyzer.component';
import { TJobExecsManagerComponent } from './elastest-etm/tjob-exec/tjob-execs-manager/tjob-execs-manager.component';
import { ProjectManagerComponent } from './elastest-etm/project/project-manager/project-manager.component';
import { TestEngineViewComponent } from './elastest-test-engines/test-engine-view/test-engine-view.component';
import { ElastestTestEnginesComponent } from './elastest-test-engines/elastest-test-engines.component';
import { ServiceDetailComponent } from './elastest-esm/support-services/service-detail/service-detail.component';
import { ServiceGuiComponent } from './elastest-esm/support-services/service-gui/service-gui.component';
import { InstancesManagerComponent } from './elastest-esm/support-services/instance-manager/instances-manager.component';
import { TestVncComponent } from './shared/vnc-client/test-vnc/test-vnc.component';
import { VncClientComponent } from './shared/vnc-client/vnc-client.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { UsersComponent } from './users/users.component';
import { DashboardComponent } from './elastest-etm/dashboard/dashboard.component';
import { EtmComponent } from './elastest-etm/etm.component';
import { ProjectFormComponent } from './elastest-etm/project/project-form/project-form.component';
import { ProjectsManagerComponent } from './elastest-etm/project/projects-manager/projects-manager.component';
import { SutsManagerComponent } from './elastest-etm/sut/suts-manager/suts-manager.component';
import { SutManagerComponent } from './elastest-etm/sut/sut-manager/sut-manager.component';
import { TJobFormComponent } from './elastest-etm/tjob/tjob-form/tjob-form.component';
import { SutFormComponent } from './elastest-etm/sut/sut-form/sut-form.component';
import { TJobsManagerComponent } from './elastest-etm/tjob/tjobs-manager/tjobs-manager.component';
import { TjobManagerComponent } from './elastest-etm/tjob/tjob-manager/tjob-manager.component';
import { TjobExecManagerComponent } from './elastest-etm/tjob-exec/tjob-exec-manager/tjob-exec-manager.component';
import { TOJobManagerComponent } from './elastest-etm/tojob/tojob-manager/tojob-manager.component';
import { ElastestEusComponent } from './elastest-eus/elastest-eus.component';
import { LoginComponent } from './login/login.component';
import { UsersFormComponent } from './users/form/form.component';
import { ElastestLogManagerComponent } from './elastest-log-manager/elastest-log-manager.component';
import { RefreshComponent } from './shared/refresh/refresh.component';
import { EtmTestlinkComponent } from './etm-testlink/etm-testlink.component';
import { TestProjectFormComponent } from './etm-testlink/test-project/test-project-form/test-project-form.component';
import { TestProjectComponent } from './etm-testlink/test-project/test-project.component';
import { TestPlanFormComponent } from './etm-testlink/test-plan/test-plan-form/test-plan-form.component';
import { TestCaseStepComponent } from './etm-testlink/test-case-step/test-case-step.component';
import { TestCaseStepFormComponent } from './etm-testlink/test-case-step/test-case-step-form/test-case-step-form.component';
import { BuildComponent } from './etm-testlink/build/build.component';
import { TestCaseExecsComponent } from './etm-testlink/build/test-case-execs/test-case-execs.component';
import { ExecutionComponent } from './etm-testlink/execution/execution.component';
import { ExternalTestExecutionFormComponent } from './elastest-etm/external/external-test-execution/external-test-execution-form/external-test-execution-form.component';
import { ExternalTjobComponent } from './elastest-etm/external/external-tjob/external-tjob.component';
import { ExternalTjobExecutionComponent } from './elastest-etm/external/external-tjob-execution/external-tjob-execution.component';
import { ExternalTjobExecutionNewComponent } from './elastest-etm/external/external-tjob-execution/external-tjob-execution-new/external-tjob-execution-new.component';

const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: '',
    component: EtmComponent,
    children: [
      {
        component: ProjectsManagerComponent,
        path: '',
      },
      {
        path: 'projects',
        children: [
          {
            path: '',
            component: ProjectsManagerComponent,
          },
          {
            path: 'add',
            component: ProjectFormComponent,
          },
          {
            path: 'edit/:projectId',
            component: ProjectFormComponent,
          },
          {
            path: 'edit',
            component: ProjectFormComponent,
          },
          {
            path: ':projectId',
            children: [
              {
                path: '',
                component: ProjectManagerComponent,
              },
              {
                path: 'tjob',
                children: [
                  {
                    path: 'edit/:tJobId',
                    component: TJobFormComponent,
                  },
                  {
                    path: 'new',
                    component: TJobFormComponent,
                  },
                  {
                    path: ':tJobId',
                    children: [
                      {
                        path: '',
                        component: TjobManagerComponent,
                      },
                      {
                        path: 'tjob-exec',
                        children: [
                          {
                            path: ':tJobExecId',
                            children: [
                              {
                                path: '',
                                component: TjobExecManagerComponent,
                              },
                              {
                                path: 'dashboard',
                                component: DashboardComponent,
                              },
                            ],
                          },
                        ],
                      },
                    ],
                  },
                ],
              },
              {
                path: 'sut',
                children: [
                  {
                    path: '',
                    component: SutManagerComponent,
                  },
                  {
                    path: 'edit/:sutId',
                    component: SutFormComponent,
                  },
                  {
                    path: 'new',
                    component: SutFormComponent,
                  },
                ],
              },
            ],
          },
        ],
      },
      {
        path: 'tjobs',
        children: [
          {
            path: '',
            component: TJobsManagerComponent,
          },
        ],
      },
      {
        path: 'tjobexecs',
        children: [
          {
            path: '',
            component: TJobExecsManagerComponent,
          },
        ],
      },
      {
        path: 'suts',
        children: [
          {
            path: '',
            component: SutsManagerComponent,
          },
          {
            path: 'edit/:id',
            component: SutFormComponent,
          },
          {
            path: 'edit',
            component: SutFormComponent,
          },
        ],
      },
      {
        path: 'etm-app',
        component: EtmComponent,
      },
      {
        path: 'test-engines',
        children: [
          {
            path: '',
            component: ElastestTestEnginesComponent,
          },
          {
            path: ':name',
            component: TestEngineViewComponent,
          },
        ],
      },
      {
        path: 'support-services',
        children: [
          {
            path: '',
            component: InstancesManagerComponent,
          },
          {
            path: 'service-detail/:id',
            component: ServiceDetailComponent,
          },
          {
            path: 'service-gui',
            component: ServiceGuiComponent,
          },
        ],
      },
      {
        path: 'service-gui',
        component: ServiceGuiComponent,
      },
      {
        path: 'logmanager',
        component: ElastestLogManagerComponent,
      },
      {
        path: 'loganalyzer',
        component: ElastestLogAnalyzerComponent,
      },
      {
        path: 'vnc',
        component: TestVncComponent,
      },
      {
        path: 'eus',
        component: ElastestEusComponent,
      },
      {
        path: 'refresh',
        component: RefreshComponent,
      },
      {
        path: 'help',
        component: HelpComponent,
      },
      {
        path: 'testlink',
        children: [
          {
            component: EtmTestlinkComponent,
            path: '',
          },
          {
            path: 'projects',
            children: [
              {
                path: 'new',
                component: TestProjectFormComponent,
              },
              {
                path: 'edit/:projectId',
                component: TestProjectFormComponent,
              },
              {
                path: 'edit',
                component: TestProjectFormComponent,
              },
              {
                path: ':projectId',
                children: [
                  {
                    path: '',
                    component: TestProjectComponent,
                  },
                  {
                    path: 'plans',
                    children: [
                      {
                        path: 'edit/:planId',
                        component: TestPlanFormComponent,
                      },
                      {
                        path: 'new',
                        component: TestPlanFormComponent,
                      },
                      {
                        path: ':planId',
                        children: [
                          {
                            path: '',
                            component: TestPlanComponent,
                          },
                          {
                            path: 'builds',
                            children: [
                              {
                                path: 'edit/:buildId',
                                component: TestPlanFormComponent,
                              },
                              {
                                path: 'new',
                                component: TestPlanFormComponent,
                              },
                              {
                                path: ':buildId',
                                children: [
                                  {
                                    path: '',
                                    component: BuildComponent,
                                  },
                                  {
                                    path: 'cases',
                                    children: [
                                      {
                                        path: ':caseId',
                                        children: [
                                          {
                                            path: '',
                                            component: TestCaseExecsComponent,
                                          },
                                        ],
                                      },
                                    ],
                                  },
                                ],
                              },
                            ],
                          },
                        ],
                      },
                    ],
                  },
                  {
                    path: 'suites',
                    children: [
                      {
                        path: 'edit/:suiteId',
                        component: TestSuiteFormComponent,
                      },
                      {
                        path: 'new',
                        component: TestSuiteFormComponent,
                      },
                      {
                        path: ':suiteId',
                        children: [
                          {
                            path: '',
                            component: TestSuiteComponent,
                          },
                          {
                            path: 'cases',
                            children: [
                              {
                                path: 'edit/:caseId',
                                component: TestCaseFormComponent,
                              },
                              {
                                path: 'new',
                                component: TestCaseFormComponent,
                              },
                              {
                                path: ':caseId',
                                children: [
                                  {
                                    path: '',
                                    children: [
                                      {
                                        path: '',
                                        component: TestCaseComponent,
                                      },
                                      {
                                        path: 'steps',
                                        children: [
                                          {
                                            path: 'edit/:stepId',
                                            component: TestCaseStepFormComponent,
                                          },
                                          {
                                            path: 'new',
                                            component: TestCaseStepFormComponent,
                                          },
                                          {
                                            path: ':stepId',
                                            children: [
                                              {
                                                path: '',
                                                component: TestCaseStepComponent,
                                              },
                                            ],
                                          },
                                        ],
                                      },
                                    ],
                                  },
                                ],
                              },
                            ],
                          },
                        ],
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      },
      {
        path: 'external',
        children: [
          {
            path: 'execute',
            component: ExternalTestExecutionFormComponent,
          },
          // {
          //     path: 'new',
          //     component: TestProjectFormComponent,
          // },
          // {
          //     path: 'edit/:projectId',
          //     component: TestProjectFormComponent,
          // },

          {
            path: 'project',
            children: [
              // {
              //     path: '',
              //     component: TestProjectComponent,
              // },
              {
                path: ':projectId',
                children: [
                  // {
                  //     path: '',
                  //     component: TestProjectComponent,
                  // },
                  {
                    path: 'tjob',
                    children: [
                      //   {
                      //     path: '',
                      //     component: ExternalTJ,
                      //   },
                      {
                        path: ':tJobId',
                        children: [
                          {
                            path: '',
                            component: ExternalTjobComponent,
                          },
                          {
                            path: 'exec',
                            children: [
                              //   {
                              //     path: '',
                              //     component: ExternalTJ,
                              //   },
                              {
                                path: 'new',
                                component: ExternalTjobExecutionNewComponent,
                              },
                              {
                                path: ':execId',
                                children: [
                                  {
                                    path: '',
                                    component: ExternalTjobExecutionComponent,
                                  },
                                ],
                              },
                            ],
                          },
                        ],
                      },
                    ],
                  },
                  {
                    path: 'sut',
                    children: [
                      {
                        path: '',
                        component: SutManagerComponent,
                      },
                      {
                        path: 'edit/:sutId',
                        component: SutFormComponent,
                      },
                      {
                        path: 'new',
                        component: SutFormComponent,
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
export const routedComponents: any[] = [
  LoginComponent,
  UsersComponent,
  UsersFormComponent,
  TJobsManagerComponent,
  ProjectsManagerComponent,
  SutManagerComponent,
  SutsManagerComponent,
  EtmComponent,
  TOJobManagerComponent,
  DashboardComponent,
  ProjectFormComponent,
  TjobManagerComponent,
  TJobFormComponent,
  TjobExecManagerComponent,
  SutFormComponent,
  ElastestEusComponent,
  ElastestLogManagerComponent,
  InstancesManagerComponent,
  ServiceGuiComponent,
  ServiceDetailComponent,
  HelpComponent,
];

export const appRoutes: any = RouterModule.forRoot(routes, { useHash: true });

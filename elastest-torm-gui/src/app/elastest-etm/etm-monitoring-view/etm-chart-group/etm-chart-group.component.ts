import { components, defaultStreamMap } from '../../../shared/defaultESData-model';
import { ESRabComplexMetricsModel } from '../../../shared/metrics-view/metrics-chart-card/models/es-rab-complex-metrics-model';
import { ElastestESService } from '../../../shared/services/elastest-es.service';
import { MetricsChartCardComponent } from '../../../shared/metrics-view/metrics-chart-card/metrics-chart-card.component';
import { ElastestRabbitmqService } from '../../../shared/services/elastest-rabbitmq.service';
import { SingleMetricModel } from '../../../shared/metrics-view/models/single-metric-model';
import { MetricsFieldModel } from '../../../shared/metrics-view/metrics-chart-card/models/metrics-field-model';
import { Subject, Observable } from 'rxjs/Rx';
import { Component, Input, OnInit, Output, QueryList, ViewChildren, EventEmitter } from '@angular/core';
import { Subscription } from 'rxjs/Rx';
import { AbstractTJobModel } from '../../models/abstract-tjob-model';
import { AbstractTJobExecModel } from '../../models/abstract-tjob-exec-model';
import { ExternalTJobExecModel } from '../../external/external-tjob-execution/external-tjob-execution-model';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';

@Component({
  selector: 'etm-chart-group',
  templateUrl: './etm-chart-group.component.html',
  styleUrls: ['./etm-chart-group.component.scss'],
})
export class EtmChartGroupComponent implements OnInit {
  @ViewChildren(MetricsChartCardComponent) MetricsChartCardComponents: QueryList<MetricsChartCardComponent>;

  @Input() public live: boolean;
  @Input() tJob: AbstractTJobModel;
  @Input() tJobExec: AbstractTJobExecModel;

  // Metrics Chart
  allInOneMetrics: ESRabComplexMetricsModel;
  metricsList: ESRabComplexMetricsModel[] = [];
  groupedMetricsList: ESRabComplexMetricsModel[][] = [];

  loaded: boolean = false;

  // TimeLine Observable
  @Output() timelineObs = new EventEmitter<any>();

  @Output() hoverObs = new EventEmitter<any>();

  @Output() leaveObs = new EventEmitter<any>();

  chartsEventsSubscriptionsObs: Subscription[] = [];

  constructor(private elastestESService: ElastestESService, private elastestRabbitmqService: ElastestRabbitmqService) {}

  ngOnInit() {}

  ngAfterViewInit(): void {
    this.MetricsChartCardComponents.changes.subscribe((data) => this.subscribeAllToEvents());
    if (this.live) {
      this.initObservables();
    }
  }

  initObservables(): void {
    // Get default Rabbit queues
    let subjectMap: Map<string, Subject<string>> = this.elastestRabbitmqService.subjectMap;
    subjectMap.forEach((obs: Subject<string>, key: string) => {
      let subjectData: any = this.elastestRabbitmqService.getDataFromSubjectName(key);
      if (subjectData.streamType === 'composed_metrics' || subjectData.streamType === 'atomic_metric') {
        obs.subscribe((data) => this.updateMetricsData(data));
      }
    });
  }

  getIgnoreComponent(): string {
    return this.tJob.hasSut() ? '' : 'sut'; // if is without sut, ignore sut metrics
  }

  initAIO(): void {
    let ignoreComponent: string = this.getIgnoreComponent();
    this.allInOneMetrics = new ESRabComplexMetricsModel(this.elastestESService, ignoreComponent);
    this.allInOneMetrics.name = 'All Metrics';
    this.allInOneMetrics.hidePrevBtn = !this.live;
    this.allInOneMetrics.metricsIndex = this.tJobExec.monitoringIndex;
    let defaultMetricName: string = 'test' + '_' + 'et_dockbeat' + '_' + 'cpu_totalUsage'; // Activate Test cpu usage as default in AIO
    this.allInOneMetrics.activateAndApplyByName(defaultMetricName);
    if (!this.live) {
      this.allInOneMetrics.getAllMetrics();
    }
  }

  initMetricsView(tJob: AbstractTJobModel, tJobExec: AbstractTJobExecModel): void {
    this.tJob = tJob;
    this.tJobExec = tJobExec;

    if (this.tJob.execDashboardConfigModel.showAllInOne) {
      this.initAIO();
    }

    for (let metric of this.tJob.execDashboardConfigModel.allMetricsFields.fieldsList) {
      if (metric.activated) {
        let individualMetrics: ESRabComplexMetricsModel = this.initializeBasicAttrByMetric(metric);
        individualMetrics.metricsIndex = this.tJobExec.monitoringIndex;
        if (metric.component === '') {
          // If no component, is a default metric
          individualMetrics.activateAllMatchesByNameSuffix(metric.name);
          if (!this.live) {
            individualMetrics.getAllMetrics();
          }
          this.metricsList.push(individualMetrics);
        } else {
          // Else, is a custom metric
          let pos: number = this.initCustomMetric(metric, individualMetrics);
          if (!this.live && pos >= 0) {
            let metricName: string = metric.streamType === 'atomic_metric' ? metric.etType : metric.etType + '.' + metric.subtype;
            this.elastestESService
              .searchAllDynamic(individualMetrics.metricsIndex, metric.stream, metric.component, metricName)
              .subscribe((obj) => this.metricsList[pos].addSimpleMetricTraces(obj.data), (error) => console.log(error));
          }
        }
      }
    }
    this.createGroupedMetricList();
  }

  initCustomMetric(metric: MetricsFieldModel, individualMetrics: ESRabComplexMetricsModel): number {
    if (metric.unit) {
      individualMetrics.yAxisLabelLeft = metric.unit;
    }

    this.metricsList.push(individualMetrics);
    this.createGroupedMetricList();

    individualMetrics.allMetricsFields.addMetricsFieldToList(
      metric,
      individualMetrics.component,
      individualMetrics.stream,
      metric.streamType,
      metric.activated,
    );
    this.tJob.execDashboardConfigModel.allMetricsFields.addMetricsFieldToList(
      metric,
      individualMetrics.component,
      individualMetrics.stream,
      metric.streamType,
      metric.activated,
    );

    let pos: number = this.metricsList.length - 1;

    if (this.live) {
      this.elastestRabbitmqService.createSubject(metric.streamType, individualMetrics.component, metric.stream);
      let index: string = this.getAbstractTJobExecIndex(individualMetrics.component);

      this.elastestRabbitmqService
        .createAndSubscribeToTopic(index, metric.streamType, individualMetrics.component, metric.stream)
        .subscribe(
          (data: any) => {
            if (data['et_type'] === metric.etType && data.component === metric.component) {
              let parsedData: SingleMetricModel = this.elastestESService.convertToMetricTrace(data, metric);
              if (parsedData === undefined) {
                console.error('Undefined data received, not added to ' + metric.name);
              } else {
                if (this.metricsList[pos]) {
                  this.metricsList[pos].addDataToSimpleMetric(metric, [parsedData]);
                }
              }
            }
          },
          (error) => console.log(error),
        );
    }
    return pos;
  }

  addMoreMetrics(obj: any): boolean {
    let metric: MetricsFieldModel = obj.metricFieldModel;
    let individualMetrics: ESRabComplexMetricsModel = this.initializeBasicAttrByMetric(metric);

    if (!this.alreadyExist(individualMetrics.name)) {
      individualMetrics.addSimpleMetricTraces(obj.data);
      individualMetrics.metricsIndex = this.tJobExec.monitoringIndex;
      this.initCustomMetric(metric, individualMetrics);

      return true;
    } else {
      return false;
    }
  }

  initializeBasicAttrByMetric(metric: any): ESRabComplexMetricsModel {
    let ignoreComponent: string = this.getIgnoreComponent();
    let individualMetrics: ESRabComplexMetricsModel = new ESRabComplexMetricsModel(this.elastestESService, ignoreComponent);
    individualMetrics.name = this.createName(metric.component, metric.stream, metric.etType, metric.subtype);
    individualMetrics.component = metric.component;
    individualMetrics.stream = metric.stream;
    individualMetrics.hidePrevBtn = !this.live;
    return individualMetrics;
  }

  createName(component: string, stream: string, etType: string, subtype: string): string {
    return this.createNameWithoutSubtype(component, stream, etType) + ' ' + subtype;
  }

  createNameWithoutSubtype(component: string, stream: string, etType: string): string {
    return component + ' ' + stream + ' ' + etType;
  }

  alreadyExist(name: string): boolean {
    for (let metric of this.metricsList) {
      if (metric.name === name) {
        return true;
      }
    }
    return false;
  }

  createGroupedMetricList(): void {
    let defaultGroupNum: number = 2;
    this.groupedMetricsList = this.createGroupedArray(this.metricsList, defaultGroupNum);
  }

  createGroupedArray(arr, chunkSize): any[] {
    let groups: any[] = [];
    let i: number;
    for (i = 0; i < arr.length; i += chunkSize) {
      groups.push(arr.slice(i, i + chunkSize));
    }
    return groups;
  }

  updateMetricsData(data: any): void {
    for (let group of this.groupedMetricsList) {
      for (let metric of group) {
        if (metric.isDefault()) {
          // Only update default metrics
          metric.updateData(data);
        }
      }
    }

    if (this.allInOneMetrics) {
      this.allInOneMetrics.updateData(data);
    }
  }

  ngAfterViewChecked() {
    if (!this.loaded) {
      this.subscribeAllToEvents();
    }
  }

  subscribeAllToEvents(): void {
    if (!this.live) {
      // If not is live, subscribe to events
      this.unsubscribeAllEvents();
      this.loaded = this.MetricsChartCardComponents.toArray() && this.MetricsChartCardComponents.toArray().length > 0;
      if (this.loaded) {
        this.MetricsChartCardComponents.forEach((element: MetricsChartCardComponent) => {
          this.subscribeToEvents(element);
        });
      }
    }
  }

  unsubscribeAllEvents(): void {
    for (let subscription of this.chartsEventsSubscriptionsObs) {
      subscription.unsubscribe();
    }
    this.chartsEventsSubscriptionsObs = [];
  }

  subscribeToEvents(element: MetricsChartCardComponent): void {
    let eventSubscription: Subscription;
    eventSubscription = element.getTimelineSubscription().subscribe((data) => {
      this.updateTimeline(data);
      this.timelineObs.next(data);
    });

    this.chartsEventsSubscriptionsObs.push(eventSubscription);

    eventSubscription = element.getHoverSubscription().subscribe((data) => {
      this.hoverCharts(data);
      this.hoverObs.next(data.value);
    });

    this.chartsEventsSubscriptionsObs.push(eventSubscription);

    eventSubscription = element.getLeaveSubscription().subscribe((data) => {
      this.leaveCharts();
      this.leaveObs.next();
    });

    this.chartsEventsSubscriptionsObs.push(eventSubscription);
  }

  updateTimeline(domain) {
    this.MetricsChartCardComponents.forEach((element) => {
      element.updateDomain(domain);
    });
  }

  hoverCharts(item) {
    this.MetricsChartCardComponents.forEach((element) => {
      element.hoverCharts(item);
    });
  }

  leaveCharts() {
    this.MetricsChartCardComponents.forEach((element) => {
      element.leaveCharts();
    });
  }

  removeAndUnsubscribe(pos: number): void {
    let lastMetric: boolean = false;
    if (this.metricsList.length === 1) {
      lastMetric = true;
    }

    let component: string = this.metricsList[pos].component;
    let stream: string = this.metricsList[pos].stream;
    let name: string = this.metricsList[pos].name;

    // If is live and is the last metric card, unsubscribe
    if (this.live && lastMetric && !this.allInOneMetrics) {
      this.unsubscribe(component, stream);
    }
    this.metricsList.splice(pos, 1);
    this.createGroupedMetricList();

    this.tJob.execDashboardConfigModel.allMetricsFields.disableMetricFieldByTitleName(name);
  }

  removeAndUnsubscribeAIO(): void {
    if (this.live && this.metricsList.length === 0) {
      this.unsubscribe(this.allInOneMetrics.component, this.allInOneMetrics.stream);
    }
    this.allInOneMetrics = undefined;
  }

  unsubscribe(component: string, stream: string): void {
    let streamType: string = 'composed_metrics';

    if (!stream || stream === '') {
      stream = defaultStreamMap.metrics;
    }

    if (!component || component === '') {
      for (component of components) {
        let index: string = this.getAbstractTJobExecIndex(component);
        this.elastestRabbitmqService.unsuscribeFromTopic(index, streamType, component, stream);
      }
    } else {
      let index: string = this.getAbstractTJobExecIndex(component);
      this.elastestRabbitmqService.unsuscribeFromTopic(index, streamType, component, stream);
    }
  }

  loadLastTraces(): void {
    for (let chart of this.metricsList) {
      chart.loadLastTraces();
    }
  }

  getAbstractTJobExecIndex(component: string): string {
    let index: string;
    switch (this.tJobExec.getAbstractTJobExecClass()) {
      case 'ExternalTJobExecModel':
        let externalTJobExec: ExternalTJobExecModel = this.tJobExec as ExternalTJobExecModel;
        index = externalTJobExec.getCurrentMonitoringIndex(component);
        break;
      case 'TJobExecModel':
        let tJobExec: TJobExecModel = this.tJobExec as TJobExecModel;
        index = tJobExec.getCurrentMonitoringIndex(component);
        break;
      default:
        // Abstract
        break;
    }
    return index;
  }
}

import { TdDigitsPipe, TdLoadingService } from '@covalent/core';
import { ColorSchemeModel } from './models/color-scheme-model';
import { SingleMetricModel } from './models/single-metric-model';
import { LoadPreviousModel } from '../load-previous-view/load-previous-model';

export interface MetricsViewModel extends LoadPreviousModel {
  name: string;
  colorScheme: ColorSchemeModel;
  gradient: boolean;

  showXAxis: boolean;
  showYAxis: boolean;
  showLegend: boolean;

  showXAxisLabel: boolean;
  xAxisLabel: string;

  showYAxisLabel: boolean;
  yAxisLabel: string;

  autoScale: boolean;

  timeline: boolean;
  data: SingleMetricModel[];
  type: string;

  prevTraces: any[];
  prevLoaded: boolean;
  hidePrevBtn: boolean;

  // ngx transform using covalent digits pipe
  axisDigits(val: any);
  loadPrevious();
}
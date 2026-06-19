export interface IconElement {
  tag: string
  [attr: string]: string
}

export interface IconDef {
  viewBox?: string
  fill?: string
  stroke?: string
  strokeWidth?: string
  elements: IconElement[]
}

const E = (tag: string, attrs: Record<string, string>): IconElement => ({ tag, ...attrs })
const P = (d: string): IconElement => E('path', { d })

export const DEFAULT_ATTRS = {
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: '1.5',
}

const FILL_ATTRS = {
  viewBox: '0 0 24 24',
  fill: 'currentColor',
  stroke: 'none',
}

export const ICONS: Record<string, IconDef> = {
  /* ──── Rail toggles ──── */
  'rail-left-collapsed': {
    ...FILL_ATTRS,
    elements: [
      P('M17.404 13.096L22 8.5l-4.596-4.596-1.414 1.414L19.172 8.5 15.99 11.682l1.414 1.414z'),
      P('M21 18v2H3v-2h18zM12 11v2H3v-2h9zm0-7v2H3V4h9z'),
    ],
  },
  'rail-left-expanded': {
    ...FILL_ATTRS,
    elements: [
      P('M21 18v2H3v-2h18zM20.01 5.318L16.828 8.5l3.182 3.182-1.414 1.414L14 8.5l4.596-4.596 1.414 1.414zM12 11v2H3v-2h9zm0-7v2H3V4h9z'),
    ],
  },
  'rail-right-collapsed': {
    ...FILL_ATTRS,
    elements: [
      P('M20.01 5.318L16.828 8.5l3.182 3.182-1.414 1.414L14 8.5l4.596-4.596 1.414 1.414zM12 11v2H3v-2h9zm0-7v2H3V4h9zM21 18v2H3v-2h18z'),
    ],
  },
  'rail-right-expanded': {
    ...FILL_ATTRS,
    elements: [
      P('M17.404 13.096L22 8.5l-4.596-4.596-1.414 1.414L19.172 8.5 15.99 11.682l1.414 1.414z'),
      P('M21 18v2H3v-2h18zM12 11v2H3v-2h9zm0-7v2H3V4h9z'),
    ],
  },

  /* ──── Left nav ──── */
  dashboard: {
    elements: [
      E('rect', { x: '3', y: '3', width: '7', height: '7', rx: '1' }),
      E('rect', { x: '14', y: '3', width: '7', height: '7', rx: '1' }),
      E('rect', { x: '3', y: '14', width: '7', height: '7', rx: '1' }),
      E('rect', { x: '14', y: '14', width: '7', height: '7', rx: '1' }),
    ],
  },
  review: {
    elements: [
      P('M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2'),
      E('rect', { x: '9', y: '3', width: '6', height: '4', rx: '1' }),
      P('m9 14 2 2 4-4'),
    ],
  },
  classes: {
    elements: [
      P('M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2'),
      E('circle', { cx: '9', cy: '7', r: '4' }),
      P('M22 21v-2a4 4 0 0 0-3-3.87'),
      P('M16 3.13a4 4 0 0 1 0 7.75'),
    ],
  },
  assignments: {
    elements: [
      P('M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z'),
      E('polyline', { points: '14 2 14 8 20 8' }),
      E('line', { x1: '16', y1: '13', x2: '8', y2: '13' }),
      E('line', { x1: '16', y1: '17', x2: '8', y2: '17' }),
    ],
  },

  /* ──── Right rail ──── */
  refresh: {
    elements: [
      E('polyline', { points: '23 4 23 10 17 10' }),
      P('M20.49 15a9 9 0 1 1-2.12-9.36L23 10'),
    ],
  },
  'sort-time': {
    elements: [
      E('circle', { cx: '12', cy: '12', r: '10' }),
      E('polyline', { points: '12 6 12 12 16 14' }),
    ],
  },
  'sort-completion': {
    elements: [
      P('M9 11l3 3L22 4'),
      P('M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11'),
    ],
  },
  'sort-count': {
    elements: [
      E('line', { x1: '4', y1: '9', x2: '20', y2: '9' }),
      E('line', { x1: '4', y1: '15', x2: '20', y2: '15' }),
      E('line', { x1: '8', y1: '5', x2: '6', y2: '19' }),
      E('line', { x1: '16', y1: '5', x2: '14', y2: '19' }),
    ],
  },
  'sort-rate': {
    elements: [
      E('rect', { x: '2', y: '2', width: '20', height: '20', rx: '2' }),
      P('M7 16l3-6 4 4 3-8'),
    ],
  },
  'filter-pending': {
    elements: [
      E('circle', { cx: '12', cy: '12', r: '10' }),
      E('polyline', { points: '8 12 11 15 16 9' }),
    ],
  },
  'filter-none': {
    elements: [
      E('circle', { cx: '12', cy: '12', r: '10' }),
      E('line', { x1: '8', y1: '12', x2: '16', y2: '12' }),
    ],
  },
  'filter-unsub': {
    elements: [
      E('circle', { cx: '12', cy: '12', r: '10' }),
      E('line', { x1: '8', y1: '8', x2: '16', y2: '16' }),
      E('line', { x1: '16', y1: '8', x2: '8', y2: '16' }),
    ],
  },

  /* ──── Dashboard toggles ──── */
  'dash-work': {
    elements: [
      P('M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z'),
      E('polyline', { points: '14 2 14 8 20 8' }),
      E('line', { x1: '16', y1: '13', x2: '8', y2: '13' }),
    ],
  },
  'dash-score': {
    elements: [
      E('line', { x1: '4', y1: '20', x2: '18', y2: '20' }),
      E('polyline', { points: '6 20 6 14 10 10 14 16 18 8' }),
    ],
  },
  'dash-dev': {
    elements: [E('polyline', { points: '22 12 18 12 15 21 9 3 6 12 2 12' })],
  },
  'dash-tw': {
    elements: [
      E('circle', { cx: '12', cy: '12', r: '10' }),
      E('circle', { cx: '12', cy: '12', r: '4' }),
      E('line', { x1: '12', y1: '2', x2: '12', y2: '8' }),
    ],
  },
  'dash-trend': {
    elements: [E('polyline', { points: '2 18 6 10 10 14 14 6 18 12 22 12' })],
  },

  /* ──── User menu ──── */
  settings: {
    elements: [
      E('circle', { cx: '12', cy: '12', r: '3' }),
      P('M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z'),
    ],
  },
  reset: {
    elements: [
      E('polyline', { points: '1 4 1 10 7 10' }),
      E('polyline', { points: '23 20 23 14 17 14' }),
      P('M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15'),
    ],
  },
  debug: {
    elements: [E('polygon', { points: '13 2 3 14 12 14 11 22 21 10 12 10 13 2' })],
  },
  sun: {
    elements: [
      E('circle', { cx: '12', cy: '12', r: '5' }),
      E('line', { x1: '12', y1: '1', x2: '12', y2: '3' }),
      E('line', { x1: '12', y1: '21', x2: '12', y2: '23' }),
      E('line', { x1: '4.22', y1: '4.22', x2: '5.64', y2: '5.64' }),
      E('line', { x1: '18.36', y1: '18.36', x2: '19.78', y2: '19.78' }),
      E('line', { x1: '1', y1: '12', x2: '3', y2: '12' }),
      E('line', { x1: '21', y1: '12', x2: '23', y2: '12' }),
      E('line', { x1: '4.22', y1: '19.78', x2: '5.64', y2: '18.36' }),
      E('line', { x1: '18.36', y1: '5.64', x2: '19.78', y2: '4.22' }),
    ],
  },
  moon: {
    elements: [P('M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z')],
  },
  logout: {
    elements: [
      P('M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4'),
      E('polyline', { points: '16 17 21 12 16 7' }),
      E('line', { x1: '21', y1: '12', x2: '9', y2: '12' }),
    ],
  },
}

// Shared icons: sort-class = classes, dash-class = dashboard
ICONS['sort-class'] = ICONS.classes
ICONS['dash-class'] = ICONS.dashboard

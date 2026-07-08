import * as artifact from '/Users/niou9/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/@oai/artifact-tool/dist/artifact_tool.mjs';

const EMU = 914400;
const toEmu = (px) => Math.round((px / 1280) * 13.333333 * EMU);
const toEmuY = (px) => Math.round((px / 720) * 7.5 * EMU);

export async function slide01(presentation, ctx) {
  const slide = presentation.slides.add();
  slide.background.fill = { type: 1, color: { type: 1, value: 'F7F2E8' }, gradientStops: [] };

  const box = slide.shapes.add({ geometry: 'rect', position: { left: 80, top: 80, width: 900, height: 120 }, fill: '#F7F2E8' });
  box.data.type = 1;
  box.data.textStyle = { anchor: 2, vertical: 1, topInset: 0, bottomInset: 0, leftInset: 0, rightInset: 0, wrap: 2 };
  box.data.paragraphs = [
    {
      id: '',
      runs: [
        {
          id: '',
          text: '教学评价系统',
          textStyle: {
            bold: true,
            italic: false,
            fontSize: 2600,
            fill: { type: 1, color: { type: 1, value: '16324F' }, gradientStops: [] },
            underline: 'none',
            typeface: 'Noto Sans SC'
          },
          citations: [],
          reviewMarkIds: []
        }
      ],
      textStyle: { alignment: 1 },
      indent: 0,
      inlineNodes: [
        {
          textRun: {
            text: '教学评价系统',
            textStyle: {
              bold: true,
              italic: false,
              fontSize: 2600,
              fill: { type: 1, color: { type: 1, value: '16324F' }, gradientStops: [] },
              underline: 'none',
              typeface: 'Noto Sans SC'
            },
            citations: [],
            reviewMarkIds: []
          }
        }
      ],
      paragraphStyle: { indent: 0, lineSpacingPercent: 110000, tabStops: [] }
    }
  ];
  return slide;
}

import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatChip from '../components/StatChip.vue'

describe('StatChip', () => {
  it('renders slot content', () => {
    const wrapper = mount(StatChip, { slots: { default: '测试文本' } })
    expect(wrapper.text()).toContain('测试文本')
  })

  it('applies push class when push prop is true', () => {
    const wrapper = mount(StatChip, { props: { push: true } })
    expect(wrapper.classes()).toContain('stat-chip--push')
  })

  it('shows tooltip via data attribute', () => {
    const wrapper = mount(StatChip, { props: { tooltip: '提示信息' } })
    expect(wrapper.attributes('data-tooltip')).toBe('提示信息')
  })
})

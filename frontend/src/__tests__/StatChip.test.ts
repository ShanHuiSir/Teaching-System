import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatChip from '../components/StatChip.vue'

describe('StatChip', () => {
  it('renders slot content', () => {
    const wrapper = mount(StatChip, { slots: { default: '测试文本' } })
    expect(wrapper.text()).toContain('测试文本')
  })

  it('shows tooltip via title attribute', () => {
    const wrapper = mount(StatChip, { props: { tooltip: '提示信息' } })
    expect(wrapper.attributes('title')).toBe('提示信息')
  })
})

/**
 * AttendEase — Chart.js Theme Configuration
 * Consistent Burgundy Glassmorphism Palette for all dashboards and analytics.
 */

const ChartTheme = {
  colors: {
    burgundy: '#641B32',
    rose: '#8F304F',
    roseMuted: '#C47A91',
    blush: '#E9C8D1',
    wineDark: '#260B18',
    success: '#75C9A3',
    warning: '#E9B96E',
    danger: '#F07889',
    glassGrid: 'rgba(255, 255, 255, 0.08)',
    textMuted: '#D0B8C0',
    textLight: '#FFF7F8'
  },

  applyDefaults: function() {
    if (typeof Chart === 'undefined') return;

    Chart.defaults.color = this.colors.textMuted;
    Chart.defaults.font.family = "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";
    Chart.defaults.font.size = 12;

    // Tooltip glass styling
    Chart.defaults.plugins.tooltip.backgroundColor = 'rgba(38, 11, 24, 0.95)';
    Chart.defaults.plugins.tooltip.titleColor = '#FFF7F8';
    Chart.defaults.plugins.tooltip.bodyColor = '#D0B8C0';
    Chart.defaults.plugins.tooltip.borderColor = 'rgba(255, 255, 255, 0.2)';
    Chart.defaults.plugins.tooltip.borderWidth = 1;
    Chart.defaults.plugins.tooltip.cornerRadius = 10;
    Chart.defaults.plugins.tooltip.padding = 10;
    Chart.defaults.plugins.tooltip.displayColors = true;

    // Legend styling
    Chart.defaults.plugins.legend.labels.color = this.colors.textLight;
    Chart.defaults.plugins.legend.labels.font = {
      family: "'Plus Jakarta Sans', sans-serif",
      weight: 600,
      size: 12
    };
  },

  createGradient: function(ctx, colorStart, colorEnd) {
    const gradient = ctx.createLinearGradient(0, 0, 0, 300);
    gradient.addColorStop(0, colorStart);
    gradient.addColorStop(1, colorEnd);
    return gradient;
  }
};

// Initialize theme defaults on script load
if (typeof Chart !== 'undefined') {
  ChartTheme.applyDefaults();
}


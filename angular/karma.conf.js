const { LOG_DEBUG } = require('karma/lib/constants');

module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-sonarqube-unit-reporter'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma'),
    ],
    client: {
      clearContext: false,
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage/'),
      subdir: '.',
      reporters: [{ type: 'html' }, { type: 'lcovonly' }],
    },
    reporters: ['progress', 'kjhtml', 'coverage', 'sonarqubeUnit'],
    sonarQubeUnitReporter: {
      sonarQubeVersion: 'LATEST',
      outputFile: 'reports/test-report.xml',
      useBrowserName: false,
    },
    port: 9876,
    colors: true,
    logLevel: LOG_DEBUG,
    autoWatch: true,
    browsers: ['ChromeHeadless'],
    singleRun: false,
    restartOnFileChange: true,
  });
};

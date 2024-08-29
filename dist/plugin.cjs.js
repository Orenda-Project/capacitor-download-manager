'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var core = require('@capacitor/core');

exports.OutputFormats = void 0;
(function (OutputFormats) {
    OutputFormats["JPEG"] = "JPEG";
    OutputFormats["PDF"] = "PDF";
    OutputFormats["BOTH"] = "BOTH";
})(exports.OutputFormats || (exports.OutputFormats = {}));

const DownloadManager = core.registerPlugin('DownloadManager', {
    web: () => Promise.resolve().then(function () { return web; }).then(m => new m.DownloadManagerWeb()),
});

class DownloadManagerWeb extends core.WebPlugin {
    startScan() {
        console.error('Document scanning is not supported on the web platform.');
        return Promise.resolve({ images: undefined, pdf: null });
    }
    startDownload(options) {
        return Promise.resolve({ value: options.url });
    }
    getDownloadList() {
        return Promise.resolve({ value: '[]' });
    }
    removeDownloads(options) {
        return Promise.resolve({ value: options.value });
    }
    resumeDownloads() {
        return Promise.resolve({ value: '[]' });
    }
}

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    DownloadManagerWeb: DownloadManagerWeb
});

exports.DownloadManager = DownloadManager;
//# sourceMappingURL=plugin.cjs.js.map

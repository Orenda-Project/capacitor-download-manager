var capacitorDownloadManager = (function (exports, core) {
    'use strict';

    const DownloadManager = core.registerPlugin('DownloadManager', {
        web: () => Promise.resolve().then(function () { return web; }).then(m => new m.DownloadManagerWeb()),
    });

    class DownloadManagerWeb extends core.WebPlugin {
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

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map

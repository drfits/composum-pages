(function (window) {
    window.composum = window.composum || {};
    window.composum.pages = window.composum.pages || {};

    (function (pages, core) {
        'use strict';

        pages.PageView = Backbone.View.extend({

            initialize: function (options) {
                this.$frame = this.$('.' + this.getCssBase());
                this.registerEventHandlers();
            },

            getCssBase: function () {
                return pages.const.frameClass;
            },

            registerEventHandlers: function () {
            }
        });

        pages.EditFrame = pages.PageView.extend({

            initialize: function (options) {
                pages.PageView.prototype.initialize.apply(this, [options]);
            },

            registerEventHandlers: function () {
                $(document).on('component:select.EditFrame', _.bind(this.selectComponent, this));
                $(document).on('component:selected.EditFrame', _.bind(this.onComponentSelected, this));
                $(document).on('component:changed.EditFrame', _.bind(this.onComponentChanged, this));
                $(document).on('component:deleted.EditFrame', _.bind(this.onComponentDeleted, this));
                $(document).on('page:selected.EditFrame', _.bind(this.onPageSelected, this));
                $(document).on('page:select.EditFrame', _.bind(this.selectPage, this));
                $(document).on('site:select.EditFrame', _.bind(this.selectSite, this));
                pages.PageView.prototype.registerEventHandlers.apply(this);
                var initialPath = this.$el.data('path');
                if (initialPath) {
                    $(document).trigger("page:select", [initialPath]);
                }
                this.$frame.on('load.EditFrame', _.bind(this.onFrameLoad, this));
                core.unauthorizedDelegate = pages.authorize;
            },

            onPageSelected: function (event, path) {
                if (this.currentPath != path) {
                    console.log('pages.EditFrame.onPageSelected(' + path + ')');
                    this.currentPath = path;
                    if (history.replaceState) {
                        history.replaceState(path, 'pages', core.getContextUrl('/bin/pages.html' + path));
                    }
                }
            },

            onFrameLoad: function (event) {
                if (!this.busy) {
                    this.busy = true;
                    var url = event.currentTarget.contentDocument.URL;
                    core.ajaxGet('/bin/cpm/nodes/node.resolve.json', {
                            data: {
                                url: url
                            }
                        }, _.bind(function (data) {
                            if (this.currentPath != data.path) {
                                console.log('pages.EditFrame.onFrameLoad(' + url + '): ' + data.path);
                                pages.current.page = data.path;
                                $(document).trigger("page:selected", [data.path]);
                            } else {
                                var select = this.selectOnLoad;
                                if (!select) {
                                    select = pages.toolbars.pageToolbar.getSelectedComponent();
                                }
                                if (select) {
                                    this.selectComponent(undefined, select.name, select.path, select.type);
                                }
                            }
                            this.selectOnLoad = undefined;
                        }, this), undefined, _.bind(function (data) {
                            this.busy = false;
                        }, this)
                    );
                } else {
                    this.busy = false;
                }
            },

            selectSite: function (event, path) {
                if (pages.current.site != path) {
                    console.log('pages.EditFrame.selectSite(' + path + ')');
                    pages.current.site = path;
                    $(document).trigger("site:selected", [path]);
                }
                this.selectPage(event, path);
            },

            selectPage: function (event, path, parameters) {
                if (pages.current.page != path) {
                    console.log('pages.EditFrame.selectPage(' + path + ')');
                    pages.current.page = path;
                    this.reloadPage(parameters);
                    $(document).trigger("page:selected", [path]);
                } else {
                    $(document).trigger("path:selected", [path]);
                }
            },

            reloadPage: function (parameters) {
                if (pages.current.page) {
                    if (!parameters) {
                        parameters = {};
                    }
                    if (!parameters['pages.mode']) {
                        parameters ['pages.mode'] = pages.current.mode.toLowerCase();
                    }
                    if (!parameters['pages.locale']) {
                        parameters ['pages.locale'] = pages.current.locale;
                    }
                    var parameterString = '';
                    _.each(_.keys(parameters), function (key) {
                        parameterString += parameterString.length == 0 ? '?' : '&';
                        parameterString += encodeURIComponent(key) + '=' + encodeURIComponent(parameters[key]);
                    });
                    console.log('pages.EditFrame.reloadPage(): ' + pages.current.page);
                    this.$frame.attr('src',
                        core.getContextUrl(pages.current.page + '.html' + parameterString));
                }
            },

            selectComponent: function (event, name, path, type) {
                console.log('pages.EditFrame.selectComponent(' + path + ')');
                if (path) {
                    this.$frame[0].contentWindow.postMessage('component:select'
                        + JSON.stringify({name: name, path: path, type: type}), '*');
                } else {
                    this.$frame[0].contentWindow.postMessage('component:select'
                        + JSON.stringify({}), '*');
                }
            },

            onComponentSelected: function (event, name, path, type) {
                console.log('pages.EditFrame.onComponentSelected(' + path + ')');
                if (path) {
                    this.$frame[0].contentWindow.postMessage('component:selected'
                        + JSON.stringify({name: name, path: path, type: type}), '*');
                } else {
                    this.$frame[0].contentWindow.postMessage('component:selected'
                        + JSON.stringify({}), '*');
                }
            },

            onComponentChanged: function (event, path) {
                if (path && path.indexOf(pages.current.page) == 0) {
                    console.log('pages.EditFrame.onComponentChanged(' + path + ')');
                    this.reloadPage();
                }
            },

            onComponentDeleted: function (event, path) {
                if (path && path.indexOf(pages.current.page) == 0) {
                    console.log('pages.EditFrame.onComponentDeleted(' + path + ')');
                    this.reloadPage();
                }
            }
        });

        pages.editFrame = core.getView('.' + pages.const.frameWrapperClass, pages.EditFrame);

    })(window.composum.pages, window.core);
})(window);

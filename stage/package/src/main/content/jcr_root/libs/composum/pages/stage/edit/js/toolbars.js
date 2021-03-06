(function (window) {
    window.composum = window.composum || {};
    window.composum.pages = window.composum.pages || {};
    window.composum.pages.toolbars = window.composum.pages.toolbars || {};

    (function (toolbars, pages, core) {
        'use strict';

        toolbars.const = _.extend(toolbars.const || {}, {
            editActionsClass: 'composum-pages-stage-edit-actions',
            pageViewActions: 'composum-pages-stage-edit-actions_page-view',
            languageMenuLabel: 'composum-pages-stage-edit-toolbar_language-label',
            languageMenu: 'composum-pages-language-menu',
            languageMenuLink: 'composum-pages-language-menu_link',
            pageViewActionsUri: '/libs/composum/pages/stage/edit/actions/view.html',
            previewAction: 'composum-pages-stage-edit-toolbar_preview',
            editAction: 'composum-pages-stage-edit-toolbar_edit',
            componentActions: 'composum-pages-stage-edit-actions_component',
            editToolbarClass: 'composum-pages-stage-edit-toolbar',
            toolbarHandleClass: 'composum-pages-stage-edit-toolbar_handle',
            editToolbarLoadUri: '/bin/cpm/pages/edit.editToolbar'
        });

        toolbars.EditAction = Backbone.View.extend({

            initialize: function (options) {
                this.$el.click(_.bind(this.onClick, this));
            },

            onClick: function (event) {
                var name = this.toolbar.data.name;
                var path = this.toolbar.data.path;
                var type = this.toolbar.data.type;
                var action = eval(this.$el.data('action'));
                if (_.isFunction(action)) {
                    action(event, name, path, type);
                }
            }
        });

        toolbars.EditToolbar = Backbone.View.extend({

            initialize: function (options) {
                var toolbar = this;
                this.$('[data-action]').each(function () {
                    var action = core.getWidget(toolbar.$el, this, toolbars.EditAction);
                    action.toolbar = toolbar;
                });
            },

            dispose: function () {
                this.$el.remove();
            }
        });

        toolbars.LocaleSelector = Backbone.View.extend({

            initialize: function (options) {
                this.$menuLabel = this.$el.closest('.' + toolbars.const.pageViewActions)
                    .find('.' + toolbars.const.languageMenuLabel);
                this.$menuItems = this.$('.' + toolbars.const.languageMenuLink);
                this.$menuItems.click(_.bind(this.onClick, this));
            },

            onClick: function (event) {
                if (event) {
                    event.preventDefault();
                }
                var $menuItem = $(event.currentTarget);
                var href = $menuItem.attr('href');
                var pattern = new RegExp('.*pages\.locale=([^&]*).*').exec(href);
                if (pages.editFrame) {
                    var key = pattern[1].toLowerCase();
                    var label = key.replace(/_/g, '.');
                    pages.editFrame.reloadPage({'pages.locale': key});
                    this.$menuLabel.text(label);
                } else {
                    location.href = href;
                }
            }
        });

        toolbars.ToolbarHandle = pages.surface.SurfaceHandle.extend({

            initialize: function (options) {
                pages.surface.SurfaceHandle.prototype.initialize.apply(this, [options]);
            },

            moveStart: function (event) {
                return _.extend(this.getPosition(event), {
                    pos: window.innerWidth - (this.toolbar.$el.position().left + this.toolbar.$el.width())
                });
            },

            move: function (event) {
                var move = this.getMove(event, this.start);
                var width = window.innerWidth;
                var right = this.start.pos - move.x;
                right = Math.round(right * 100 / width);
                if (right < 0) {
                    right = 0;
                }
                if (right > 85) {
                    right = 85;
                }
                this.toolbar.profile.position = right;
                this.toolbar.$el.css('right', right + '%');
            },

            save: function () {
                this.toolbar.saveProfile();
            },

            onClick: function (event) {
            }

        });

        toolbars.PageToolbar = Backbone.View.extend({

            initialize: function (options) {
                this.$view = this.$('.' + toolbars.const.pageViewActions);
                this.$component = this.$('.' + toolbars.const.componentActions);
                this.initPageView();
                $(document).on('page:selected.PageToolbar', _.bind(this.onPageSelected, this));
                $(document).on('component:selected.PageToolbar', _.bind(this.onComponentSelected, this));
                this.loadProfile();
                this.$el.css('right', this.profile.position + '%');
                if (pages.current.mode == pages.const.modes.edit ||
                    pages.current.mode == pages.const.modes.develop) {
                    this.onComponentSelected();
                }
            },

            initPageView: function (path) {
                this.currentPage = path;
                this.handle = core.getWidget(this.el, '.' + toolbars.const.toolbarHandleClass, toolbars.ToolbarHandle);
                this.handle.toolbar = this;
                this.$('.' + toolbars.const.previewAction).attr('href',
                    '?pages.mode=' + pages.profile.get('mode', 'preview', 'preview'));
                this.$('.' + toolbars.const.editAction).attr('href',
                    '?pages.mode=' + pages.profile.get('mode', 'edit', 'edit'));
                toolbars.localeSelector = core.getView('.' + toolbars.const.languageMenu, toolbars.LocaleSelector);
            },

            profileAspect: function () {
                return 'toolbar'
            },

            loadProfile: function () {
                this.profile = {
                    position: pages.profile.get(this.profileAspect(), 'position', 30)
                };
            },

            saveProfile: function () {
                if (this.profile) {
                    pages.profile.set(this.profileAspect(), 'position', this.profile.position);
                }
            },

            onPageSelected: function (event, path) {
                if (this.currentPage != path) {
                    console.log('toolbars.PageToolbar.onPageSelected(' + path + ')');
                    core.ajaxGet(toolbars.const.pageViewActionsUri + path, {},
                        _.bind(function (data) {
                            this.$view.html(data);
                            this.initPageView(path);
                            if (pages.current.mode == pages.const.modes.edit ||
                                pages.current.mode == pages.const.modes.develop) {
                                this.loadComponentToolbar(path);
                            }
                        }, this));
                }
            },

            getSelectedComponent: function () {
                return this.componentToolbar ? this.componentToolbar.data : undefined;
            },

            onComponentSelected: function (event, name, path, type) {
                if (this.componentToolbar) {
                    if (this.componentToolbar.data.path == path) {
                        return;
                    }
                    this.componentToolbar.dispose();
                    this.componentToolbar = undefined;
                }
                if (!path) {
                    path = this.currentPage;
                }
                console.log('toolbars.PageToolbar.onComponentSelected(' + path + ')');
                if (path) {
                    this.loadComponentToolbar(path, type);
                }
            },

            loadComponentToolbar: function (path, type) {
                if (path) {
                    core.ajaxGet(toolbars.const.editToolbarLoadUri + '.' + pages.current.mode + '.html' + path, {
                            data: {
                                type: type
                            }
                        },
                        _.bind(function (data) {
                            this.$component.html(data);
                            this.componentToolbar = core.getWidget(this.$component[0],
                                '.' + toolbars.const.editToolbarClass, toolbars.EditToolbar);
                            this.componentToolbar.data = {
                                name: name,
                                path: path,
                                type: type
                            };
                        }, this));
                }
            }
        });

        toolbars.pageToolbar = core.getView('.' + toolbars.const.editActionsClass, toolbars.PageToolbar);

    })(window.composum.pages.toolbars, window.composum.pages, window.core);
})(window);

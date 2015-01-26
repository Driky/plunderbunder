define ['react'], (React) ->
    
    { div, button, span, i, a, h4 } = React.DOM
    
    MaterialUsesPanel = React.createClass
        getInitialState: ->
            {
                reloading: false
                items: []
            }
        render: ->
            div {
                className: "modal fade" 
                id: "matlUsesPanel" 
                tabIndex: "-1" 
                role: "dialog"
                key: 'mdl'
            }, [
                div {
                    className: "modal-dialog"
                    key: 'mdlg'
                }, [
                    div { 
                        className: "modal-content" 
                        key: 'mdlc'
                    }, [
                        div {className: "modal-header", key: 'modlh' }, [
                            button {
                                type: "button"
                                className: "close"
                                'data-dismiss': "modal"
                                key: 'mdldisx'
                            }, [
                                span { key: 'x' }, "×"
                            ]
                            h4 {
                                className: "modal-title"
                                id: "materialUsesLabel"
                                key: 'mdlh'
                            }, if @state.materialName then "Uses for " + @state.materialName else "Uses for Material"
                        ]
                        div {
                            className: "modal-body"
                            key: 'mdlb'
                        }, [
                            if @state.reloading
                                "Loading..."
                            else
                                @state.items.map (item) ->
                                    div { className: 'row', key: 'row' + item.id }, [
                                        div {
                                            className: 'col-md-12'
                                            key: 'col' + item.id 
                                        }, item.name
                                    ]
                        ]
                        div {
                            className: "modal-footer"
                            key: 'modlf'
                        }, [
                            button {
                                type: "button"
                                className: "btn btn-default"
                                'data-dismiss': "modal"
                                key: 'mdldisc'
                            }, "Close"
                        ]
                    ]
                ]
            ]
            
    AssetList = React.createClass
        getInitialState: ->
            {
                assets: null
                expanded: []
            }
        reloadViaAjax: () ->
            userAssets = jsRoutes.controllers.User.getUserAssets()
            userAssets.ajax()
            .done ((result) ->
                if this.isMounted()
                    apiKeyValid = result.accessMask != null
                    @setState { assets: result.assets }
                ).bind this
            .fail ((jqXHR, textStatus, errorThrown) ->
                resultCode = jqXHR.status
                if this.isMounted()
                    @setState { assets: [] }
            ).bind this
            
        componentDidMount: ->
            @reloadViaAjax()
            
        componentWillReceiveProps: (nextProps) ->
            @reloadViaAjax()
        
        expandClicked: (event) ->
            eveItemID = Number(event.target.id.substring(2))
            ex = @state.expanded
            index = @state.expanded.indexOf(eveItemID)
            if index == -1
                ex.push(eveItemID)
                @setState { expanded: ex}
            else
                ex.splice(index, 1)
                @setState { expanded: ex}
        
        findUses: (event) ->
            eveItemID = Number(event.target.id.substring(3))
            
            $('#matlUsesPanel').modal { show: true }
            usesPanel = @refs.usesPanel
            usesPanel.setState {material: eveItemID, reloading: true, items: []}
            
            productUrl = jsRoutes.controllers.BlueprintController.productsForMaterial(eveItemID)
            
            productUrl.ajax() 
            .done ((result) ->
                if this.isMounted()
                    usesPanel.setState { reloading: false, items: result.items }
                ).bind this
            .fail ((jqXHR, textStatus, errorThrown) ->
                resultCode = jqXHR.status
                if this.isMounted()
                    @setState { reloading: false, items: [] }
            ).bind this
            
        render: ->
            materialUsesPanel = React.createFactory MaterialUsesPanel
            
            if !@state.assets
                div {key: 'assets'}, "Loading..."
            else if @state.assets.length == 0
                div {key: 'assets'}, "You have literally nothing."
            else
                assetsByLoc = {}
                
                @state.assets.map (asset) ->
                    if assetsByLoc[asset.locationID] != undefined
                        assetsByLoc[asset.locationID].push(asset)
                    else
                        assetsByLoc[asset.locationID] = [asset]
                
                locationIDs = Object.keys(assetsByLoc)
                
                expanded = @state.expanded
                displayUses = @state.displayUses
                
                expandClicked = @expandClicked
                findUses = @findUses
                
                locationGroups = locationIDs.map (location) ->
                    locAssets = assetsByLoc[location]
                    locName = locAssets[0].locationName
                    
                    assetRows = locAssets.map (asset) ->
                        baseRow = div { key: 'row' + asset.eveItemID, className: 'row bottom7'}, [
                            div { key: 'exp', className: 'col-md-1' }, [
                                if asset.contents.length > 0 then button { 
                                    key: 'plus'
                                    className: 'btn btn-xs pull-right'
                                    onClick: expandClicked
                                }, [
                                    if expanded.indexOf(asset.eveItemID) < 0
                                        i { 
                                            key: 'plusicn'
                                            className: 'glyphicon glyphicon-plus'
                                            ariaHidden: true
                                            id: 'pl' + asset.eveItemID
                                        }, null
                                    else
                                        i { 
                                            key: 'plusicn'
                                            className: 'glyphicon glyphicon-minus',
                                            ariaHidden: true
                                            id: 'pl' + asset.eveItemID
                                        }, null
                                ]
                                if asset.usedInManufacturing
                                    button {
                                        key: 'drilldown'
                                        className: 'btn btn-xs pull-right'
                                        onClick: findUses
                                    }, [
                                        i { 
                                            key: 'mag'
                                            className: 'glyphicon glyphicon-search'
                                            id: 'use' + asset.typeID 
                                        }, null
                                    ]
                                else
                                    null
                            ]
                            div { key: 'q', className: 'col-md-1', style: { textAlign: 'right' } }, asset.quantity
                            div { key: 'an', className: 'col-md-10' }, asset.assetName
                        ]
                        result = if expanded.indexOf(asset.eveItemID) >= 0
                            contentRows = asset.contents.map (content) ->
                                div { key: 'row' + content.eveItemID, className: 'row bottom7'}, [
                                    div { key: 'spc', className: 'col-md-2' }, null
                                    div { key: 'q', className: 'col-md-1', style: { textAlign: 'right' } }, " " + content.quantity
                                    div { key: 'an', className: 'col-md-9' }, content.assetName
                                ]
                            contentRows.unshift(baseRow)
                            contentRows
                        else
                            baseRow
                            
                        result
                    
                    div { 
                        key: 'loc-' + location 
                        className: 'panel panel-info'
                    }, [
                        div { 
                            key: 'title'
                            className: 'panel-heading' 
                        }, [
                            a { 
                                key: 'lnk'
                                'data-toggle': "collapse"
                                'data-target': '#pnl' + location 
                                href: '#'
                                ariaExpanded: "true" 
                                ariaControls: 'pnl' + location
                            }, locName + ': ' + locAssets.length + if locAssets.length == 1 then ' asset' else ' assets'
                        ]
                        div {
                            key: 'clps'
                            className: 'panel-collapse collapse in'
                            id: 'pnl' + location
                        }, [
                            div {
                                key: 'body'
                                className: 'panel-body'
                            }, [
                                assetRows
                            ]
                        ]
                    ]
                    
                children = locationGroups
                children.push( materialUsesPanel {key: 'up', ref: 'usesPanel'}, null )
                div {key: 'assets'}, locationGroups
    AssetList
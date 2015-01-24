define ['react'], (React) ->
    
    { div, button, span, i } = React.DOM
    
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
        
        render: ->
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
                
                expandClicked = @expandClicked
                
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
                        }, locName + ': ' + locAssets.length + if locAssets.length == 1 then ' asset' else ' assets'
                        div {
                            key: 'body'
                            className: 'panel-body'
                        }, [
                            assetRows
                        ]
                    ]
                    
                div {key: 'assets'}, locationGroups
    AssetList
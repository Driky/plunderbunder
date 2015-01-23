require.config {
    paths: {
        react: '../lib/react/react'
    }
}

require ['react'], (React) ->

    {div, h1, tr, table, td, th, button} = React.DOM

    MaintenanceStatus = React.createClass
        getInitialState: ->
            { data: this.props.data }
    
        render: -> 
            table { key: Math.random(), className: "table"}, [
                tr { key: Math.random()}, [
                    th { key: "dataset-header" }, "Dataset",
                    th { key: "last-updated-header" }, "Last Updated"
                ],
                this.state.data.map(
                    (row) ->
                        tr { key: Math.random() }, [
                            # debugger 
                            row.map(
                                (cell) ->
                                    td { key: Math.random() }, cell
                            )
                        ]
                )
            ]
    
        sdeUpdated: ->
            @refreshStateFromServer()
    
        componentDidMount: ->
            @refreshStateFromServer()
        
        refreshStateFromServer: ->
            maintStatusApi = jsRoutes.controllers.Configure.maintenanceStatus
            $.ajax maintStatusApi()
            .done ((result) ->
                if this.isMounted()
                    formatted = []
                    if result.length == 0
                        formatted = [["no data", "n/a"]]
                    else
                        formatted = result.map(
                            (row) ->
                                ld = new Date(row.lastImport)
                                [row.dataSet, ld.toString()]
                        )
                    this.setState { data: formatted }
                ).bind this
            .fail ((jqXHR, textStatus, errorThrown) ->
                resultCode = jqXHR.status
                if this.isMounted()
                    this.setState { data: [[errorThrown, "n/a"]] }
            ).bind this
        
    ReloadSDE = React.createClass
        getInitialState: -> 
            {}
    
        render: ->
            button { 
                type: 'button' 
                className: 'btn btn-default'
                onClick: @handleClick
            }, "Reload SDE"
    
        handleClick: (event) ->
            # trigger update
            updateSdeUrl = jsRoutes.controllers.Configure.reloadSde
            $.ajax updateSdeUrl()
            .done ((result) ->
                # Assuming any 200 is success
                x = @props.parent.sdeUpdated()
            ).bind(this)
            .fail ((jqXHR, textStatus, errorThrown) ->
                resultCode = jqXHR.status
                alert "Error thrown: " + errorThrown
            )
        
    ReloadNullsecStations = React.createClass
        render: -> 
            button {
                type: 'button'
                className: 'btn btn-default'
                onClick: @handleClick
            }, "Reload Contested"
        
        handleClick: (event) ->
            # trigger update
            updateNullsecUrl = jsRoutes.controllers.Configure.reloadNullsecStations
            $.ajax updateNullsecUrl()
            .done ((result) ->
                # Assuming any 200 is success
                x = @props.parent.sdeUpdated()
            ).bind(this)
            .fail ((jqXHR, textStatus, errorThrown) ->
                resultCode = jqXHR.status
                alert "Error thrown: " + errorThrown
            )
        
    Master = React.createClass
        getInitialState: -> 
            {}
    
        sdeUpdated: ->
            this.refs.maintenanceLog.sdeUpdated()
    
        render: ->
            maintenanceLog = React.createElement MaintenanceStatus, { data: [[]], ref: 'maintenanceLog' }
            reloadSDE = React.createElement ReloadSDE, { parent: this }
            reloadNullsecStations = React.createElement ReloadNullsecStations, { parent: this }
            div {}, [
                maintenanceLog,
                reloadSDE
                reloadNullsecStations
            ]
        
    masterLayout = React.createElement Master, null

    rendered = React.render masterLayout, document.getElementById('content')



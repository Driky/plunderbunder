define ['react'], (React) ->
    
    { div, button, span, i, a, h4 } = React.DOM
    { table, tr, th, td } = React.DOM
    
    MarketOrders = React.createClass
        getInitialState: ->
            {
                loading: true
                buyOrders: null
                sellOrders: null
            }
            
        render: ->
            if @state.loading
                div { key: 'modiv'}, "Loading..."
            else
                table { key: 'ot', className: 'table table-striped' }, [
                    tr { key: 'otbr', className: 'active' }, [
                        td { key: 'bd', colSpan: 4, style: { fontWeight: 'bold' } }, "Buy Orders"
                    ]
                    tr { key: 'othr', className: 'info' }, [
                        th { key: 'oid' }, "Order ID"
                        th { key: 'oiq' }, "Quantity"
                        th { key: 'oin' }, "Item"
                        th { key: 'oip', style: { textAlign: 'right' } }, "Price"
                    ]
                    @state.buyOrders.map (row) ->
                        tr { key: 'r' + row.orderID }, [
                            td { key: 'id' }, row.orderID
                            td { key: 'vol' }, row.volRemaining.toLocaleString() + ' / ' +  row.volEntered.toLocaleString()
                            td { key: 'item' }, row.typeName
                            td { key: 'pc', style: { textAlign: 'right' } }, row.price.toLocaleString()
                        ]
                    tr { key: 'otsr', className: 'active' }, [
                        td { key: 'bd', colSpan: 4, style: { fontWeight: 'bold' } }, "Sell Orders"
                    ]
                    tr { key: 'otshr', className: 'info' }, [
                        th { key: 'oid' }, "Order ID"
                        th { key: 'oiq' }, "Quantity"
                        th { key: 'oin' }, "Item"
                        th { key: 'oip', style: { textAlign: 'right' } }, "Price"
                    ]
                    @state.sellOrders.map (row) ->
                        tr { key: 'r' + row.orderID }, [
                            td { key: 'id' }, row.orderID
                            td { key: 'vol' }, row.volRemaining.toLocaleString() + ' / ' +  row.volEntered.toLocaleString()
                            td { key: 'item' }, row.typeName
                            td { key: 'pc', style: { textAlign: 'right' } }, row.price.toLocaleString()
                        ]
                    
                ]
                
        reloadViaAjax: () ->
            openOrdersURL = jsRoutes.controllers.User.getMarketOrders()
            openOrdersURL.ajax()
            .done ((result) ->
                if this.isMounted()
                    @setState { 
                        loading: false 
                        buyOrders: result.buyOrders
                        sellOrders: result.sellOrders
                    }
                ).bind this
            .fail ((jqXHR, textStatus, errorThrown) ->
                resultCode = jqXHR.status
                if this.isMounted()
                    @setState { 
                        loading: false 
                        buyOrders: null
                        sellOrders: null
                    }
            ).bind this
            
        componentDidMount: ->
            @reloadViaAjax()
            
    MarketOrders
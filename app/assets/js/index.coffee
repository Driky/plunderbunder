
asset_router = ar.controllers.Assets
        
{nav, ul, li} = React.DOM
{img, a, p, div, input, button, span} = React.DOM
{table, tr, td, th, tbody} = React.DOM

LoginButton = React.createClass
    loggedIn: ->
        this.setState { logged_in: false }

    getInitialState: ->
        {
            logged_in: false
            character: null
            ajaxLoading: true
        }
    
    renderLoggedIn: (loginPath) ->
        eve_login_img_src = (asset_router.at "images/EVE_SSO_Login_Buttons_Small_White.png").url
        loginPath = kartelConfig.eve_login
        
        li {}, [
            a {"href": loginPath}, [
                img {"src": eve_login_img_src, "key":"img"}, null
            ]
        ]
    
    render: ->
        unless this.state.ajaxLoading
            unless this.state.logged_in
                this.renderLoggedIn()
            else
                li { className: 'dropdown' }, [
                    a {href: '#', className: 'dropdown-toggle', role: 'button', 'data-toggle': 'dropdown'}, [
                        "Welcome, " + this.state.character
                        span { className: 'caret' }, null
                    ]
                    ul { className: 'dropdown-menu', role: 'menu' }, [
                        li {}, [
                            a { href: jsRoutes.controllers.Application.logout().url }, "Log Out"
                        ]
                    ]
                        
                ]
        else
            li {}, [
                a {href: '#'}, "Loading..."
            ]
    componentDidMount: ->
        userApi = jsRoutes.controllers.Application.user()
        $.ajax userApi.url
        .done ((result) ->
            resJson = $.parseJSON(result)
            if this.isMounted()
                this.setState { logged_in: true, character: resJson.character,  ajaxLoading: false}
            ).bind this
        .fail ((jqXHR, textStatus, errorThrown) ->
            resultCode = jqXHR.status
            if this.isMounted()
                this.setState { logged_in: false, character: null, ajaxLoading: false }
        ).bind this
    
login = React.createElement LoginButton, null

ItemSearch = React.createClass
    handleClick: (event) ->
        itemSearch = this.refs.itemSearchInput.getDOMNode()
        itemName = $(itemSearch).val()
        itemID = @state.selectedItem
        this.props.onItemSelected { name: itemName, id: itemID }
        
    render: ->
        div { className: 'row bottom7' }, [
            div { className: 'col-md-5' }, [
                div { className: 'input-group' }, [
                    input { 
                        className: 'form-control itemInput', 
                        ref: 'itemSearchInput', 
                        type: 'text',
                        placeholder: 'What are you building?'
                    }, null
                    span { className: 'input-group-btn' }, [
                        button { type: 'submit', ref: 'itemSearchButton', className: 'btn btn-primary', onClick: @handleClick }, "Show Details"
                    ]
                ]
            ]
        ]
    componentDidMount: ->
        itemList = jsRoutes.controllers.Application.inventoryItems().url
        
        engine = new Bloodhound { 
            datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name'),
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            prefetch: itemList 
        }
        engine.clearPrefetchCache()
        engine.initialize()
        
        itemSearchDom = this.refs.itemSearchInput.getDOMNode()
        searchButton = $(this.refs.itemSearchButton.getDOMNode())
        searchButton.prop('disabled', true)
        
        itemSearch = $(itemSearchDom)
        itemSearch.typeahead {
          minLength: 3,
          highlight: true
        },
        {
          name: 'all-items',
          source: engine.ttAdapter(),
          displayKey: 'name'
        }
        
        component = this
        itemSearch.on "typeahead:selected typeahead:autocompleted", (e, datum) ->
            component.setState { selectedItem: datum.id }
            searchButton = $(component.refs.itemSearchButton.getDOMNode())
            searchButton.prop('disabled', false)
        
        # Reset the value in the event a user changes the text but it's not auto completed
        itemSearch.on "change", (e) ->
            searchButton = $(component.refs.itemSearchButton.getDOMNode())
            searchButton.prop('disabled', true)
            component.setState { selectedItem: null }
        true


BillOfMaterialsEntry = React.createClass
    getInitialState: ->
        {
            jitaPrice: NaN
            jitaSplit: NaN
        }
    render: ->
        tr {}, [
            td { style: { textAlign: 'right' } }, @props.quantity
            td {}, @props.name
            td { style: { textAlign: 'right' } }, @props.volume
            td {}, @props.materialID
            td { style: { textAlign: 'right' } }, @state.jitaPrice.toLocaleString()
            td { style: { textAlign: 'right' } }, @state.jitaSplit.toLocaleString()
        ]
    componentDidMount: ->
        jitaPriceURL = jsRoutes.controllers.MarketController.jitaPriceForItem(@props.materialID)
        $.ajax jitaPriceURL
        .done ((result) ->
            split = Math.ceil((result.buyPrice + result.sellPrice) / 2)
            cost = result.sellPrice * @props.quantity
            splitCost = split * @props.quantity
            this.setState { jitaPrice: cost, jitaSplit: splitCost}
        ).bind(this)
        .fail((jqXHR, textStatus, errorThrown) ->
            this.setState { jitaPrice: 'n/a', jitaSplit: 'n/a'}
        )

BlueprintDetails = React.createClass
    getInitialState: ->
        {
            billOfMaterials: []
        }

    loadBillOfMaterials: (itemID) ->
        bomURL = jsRoutes.controllers.BlueprintController.materialsForProduct(itemID)
        $.ajax bomURL
        .done ((result) ->
            # Assuming any 200 is success
            # alert result
            @setState { billOfMaterials: result }
        ).bind(this)
        .fail ((jqXHR, textStatus, errorThrown) ->
            resultCode = jqXHR.status
            alert "Error thrown: " + errorThrown
        )
    
    render: ->
        bomeFactory = React.createFactory BillOfMaterialsEntry
        if (@state.billOfMaterials.length > 0)
            div { className: 'row' }, [
                div { className: 'col-md-6' }, [
                    table { className: 'table' }, [
                        tr {}, [
                            th {}, "Quantity"
                            th {}, "Item"
                            th {}, "Volume"
                            th {}, "Item ID"
                            th {}, "Jita Sell"
                            th {}, "Jita Split"
                        ]
                        tbody {}, [
                            @state.billOfMaterials.map (row) ->
                                bomeFactory row, null
                        ]
                    ]
                ]
            ]
        else 
            div null, null
    
MasterView = React.createClass
    si: (item) ->
        # alert item.name + ' / ' + item.id
        this.refs.blueprintDetail.loadBillOfMaterials(item.id)
        
    render: ->
        isf = React.createFactory ItemSearch
        bdf = React.createFactory BlueprintDetails
        lb = React.createFactory LoginButton
        div null, [
            nav { className: 'navbar navbar-inverse navbar-fixed-top' }, [
                div { className: 'navbar-header' }, [
                    a { className: 'navbar-brand', href: '#' }, "Kartel"
                ]
                ul { className: 'nav navbar-nav navbar-right' }, [
                    lb {}, null
                    li {style: { paddingRight: '15px'}}, ' '
                ]
            ]
            div null, [
                isf { onItemSelected: this.si }, null
                bdf { ref: 'blueprintDetail' }, null
            ]
        ]
    
masterView = React.createElement MasterView, null 

rendered = React.render masterView, document.getElementById('content')



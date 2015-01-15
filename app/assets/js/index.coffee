
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
                li { key: 'lb-dd', className: 'dropdown' }, [
                    a { key: 'lb-dd-a', href: '#', className: 'dropdown-toggle', role: 'button', 'data-toggle': 'dropdown'}, [
                        "Welcome, " + this.state.character
                        span { key: 'lb-dd-crt', className: 'caret' }, null
                    ]
                    ul { key: 'lb-dd-mnu', className: 'dropdown-menu', role: 'menu' }, [
                        li { key: 'lb-dd-mnu-lo' }, [
                            a { key: 'lb-dd-lo-a', href: jsRoutes.controllers.Authentication.logout().url }, "Log Out"
                        ]
                    ]

                ]
        else
            li { key: 'lb-dd2' }, [
                a { key: 'ldn', href: '#'}, "Loading..."
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
    getInitialState: ->
        {
        }

    handleClick: (event) ->
        itemSearch = this.refs.itemSearchInput.getDOMNode()
        itemName = $(itemSearch).val()
        itemID = @state.selectedItem
        this.props.onItemSelected { name: itemName, id: itemID }

    render: ->
        div { key: 'srch-row', className: 'row bottom7' }, [
            div { key: 'srch-col', className: 'col-md-5' }, [
                div { key: 'in-grp', className: 'input-group' }, [
                    input {
                        key: 'srch-inp',
                        className: 'form-control itemInput',
                        ref: 'itemSearchInput',
                        type: 'text',
                        placeholder: 'What are you building?'
                    }, null
                    span { key: 'srch-btn-spn', className: 'input-group-btn' }, [
                        button {
                            key: 'srch-btn',
                            type: 'submit',
                            ref: 'itemSearchButton',
                            className: 'btn btn-primary',
                            onClick: @handleClick
                        }, "Show Details"
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



BillOfMaterialMfgPrice = React.createClass
    getInitialState: ->
        {
            amount: NaN
        }
    
    addCost: (cost) ->
        amt = @state.amount
        if (isNaN(amt))
            amt = cost
        else
            amt += cost
            
        @setState { amount: amt }
    
    render: ->
        if (isNaN @state.amount)
            td { key: 'mfgtd' }, null
        else
            td { key: 'mfgtd' }, @state.amount.toLocaleString()

BillOfMaterialsEntry = React.createClass
    getInitialState: ->
        {
            jitaPrice: NaN
            jitaSplit: NaN
            subMaterials: []
            keyfix: Math.random()
        }

    getSubMaterialsClick: ->
        bomURL = jsRoutes.controllers.BlueprintController.materialsForProduct @props.materialID
        $.ajax bomURL
        .done ((result) ->
            # Assuming any 200 is success
            # alert result
            @setState {
                subMaterials: result
            }
        ).bind(this)
        .fail ((jqXHR, textStatus, errorThrown) ->
            resultCode = jqXHR.status
            alert "Error thrown: " + errorThrown
        )

    addMfgCosts: (a,b) ->
        r = @refs
        m = r.mfg
        c = m.addCost
        c(b)
        # @refs.mfg.addCost(b)

    render: ->
        bomeFactory = React.createFactory BillOfMaterialsEntry
        bommFactory = React.createFactory BillOfMaterialMfgPrice
        ac = @addMfgCosts
        rowClass = ''
        if (@props.materialBlueprint)
            rowClass = 'buildable-material-item'
        else
            rowClass = 'raw-material-item'

        getSubMaterials = null
        if (@props.materialBlueprint)
            getSubMaterials = td { key:@state.keyfix + '-subm-srch' }, [
                button { key:@state.keyfix + '-subm-btn', type: 'submit', ref: 'itemSearchButton', className: 'btn btn-warning btn-sm', onClick: @getSubMaterialsClick }, [
                    span { key:@state.keyfix + '-subm-icn', className: "glyphicon glyphicon-wrench" }, null
                ]

            ]
        else
            if (@props.isChild)
                rowClass = 'child-material-item'
            getSubMaterials = td { key:@state.keyfix + '-subm' }, null

        q = @props.quantity
        if (@state.subMaterials.length > 0)
            subMaterials = @state.subMaterials.map (row) ->
                row.addCosts = ac
                row.isChild = true
                row.quantity = row.quantity * q
                row.key = row.materialID
                bomeFactory row, null
        else
            subMaterials = null
            
        actualChildren = [
            td { key: 'q', style: { textAlign: 'right' } }, @props.quantity
            td { key: 'n' }, @props.name
            td { key: 'v', style: { textAlign: 'right' } }, @props.volume
            td { key: 'matl', style: { textAlign: 'center' } }, @props.materialID
            td { key: 'jsl', style: { textAlign: 'right' } }, @state.jitaPrice.toLocaleString()
            td { key: 'jspl', style: { textAlign: 'right' } }, @state.jitaSplit.toLocaleString()
            getSubMaterials
            bommFactory { key: 'mfg', ref: 'mfg' }, null
        ]
        
        actualEntry = tr { key: 'bome-ae', className: rowClass }, actualChildren

        if (@props.isChild)
            retVal = actualEntry
        else
            result = [actualEntry].concat(subMaterials)
            retVal = tbody { key: 'bomerow' }, result

        retVal

    getJitaPricing: ->
        jitaPriceURL = jsRoutes.controllers.MarketController.jitaPriceForItem(@props.materialID)
        ac = @props.addCosts
        $.ajax jitaPriceURL
        .done ((result) ->
            split = Math.ceil((result.buyPrice + result.sellPrice) / 2)
            cost = result.sellPrice * @props.quantity
            splitCost = split * @props.quantity
            this.setState { jitaPrice: cost, jitaSplit: splitCost }
            ac(cost, splitCost)

        ).bind(this)
        .fail((jqXHR, textStatus, errorThrown) ->
            this.setState { jitaPrice: 'n/a', jitaSplit: 'n/a'}
        )


    componentDidMount: ->
        this.getJitaPricing()
        true

    componentWillReceiveProps: (nextProps) ->
        @setState {
            jitaPrice: NaN
            jitaSplit: NaN
            subMaterials: []
        }
        this.getJitaPricing()
        true

BillOfMaterialsFooter = React.createClass

    getInitialState: -> {
        totalJita: 0
        totalJitaSplit: 0
    }
    addCosts: (jitaCost, jitaSplitCost) ->
        jitaTotal = @state.totalJita + jitaCost
        jitaSplitTotal = @state.totalJitaSplit + jitaSplitCost
        @setState {
            totalJita: jitaTotal
            totalJitaSplit: jitaSplitTotal
        }

    clearCosts: ->
        @setState {
            totalJita: 0
            totalJitaSplit: 0
        }

    render: ->
        tr { key: 'bomf-row' }, [
            th { key: 'bomf-ttl' }, "Total"
            th { key: 'bomf-spc', colSpan: 3}, ""
            th { key: 'bomf-ttl-jsl', style: { textAlign: 'right' } }, @state.totalJita.toLocaleString()
            th { key: 'bomf-ttl-jspl', style: { textAlign: 'right' } }, @state.totalJitaSplit.toLocaleString()
        ]

BlueprintDetails = React.createClass
    getInitialState: ->
        {
            billOfMaterials: []
        }

    addCosts: (jitaCost, jitaSplitCost) ->
        @refs.bomFooter.addCosts(jitaCost, jitaSplitCost)

    loadBillOfMaterials: (itemID) ->
        bomURL = jsRoutes.controllers.BlueprintController.materialsForProduct(itemID)
        $.ajax bomURL
        .done ((result) ->
            # Assuming any 200 is success
            # alert result
            @setState {
                billOfMaterials: result
                totalJita: 0
                totalJitaSplit: 0
            }
        ).bind(this)
        .fail ((jqXHR, textStatus, errorThrown) ->
            resultCode = jqXHR.status
            alert "Error thrown: " + errorThrown
        )

    render: ->
        bomeFactory = React.createFactory BillOfMaterialsEntry
        bomfFactory = React.createFactory BillOfMaterialsFooter

        if (@state.billOfMaterials.length > 0)
            ac = @addCosts
            i = 0
            div { key: 'bomrow' + Math.random(), className: 'row' }, [
                div { key: 'bomcol', className: 'col-md-8' }, [
                    table { key: 'bomtable', className: 'table' }, [
                        tr { key: 'bomheader' }, [
                            th { key: 'bomheader-q', style: { textAlign: 'center' } }, "Quantity"
                            th { key: 'bomheader-i' }, "Item"
                            th { key: 'bomheader-v', style: { textAlign: 'center' } }, "Volume"
                            th { key: 'bomheader-iid', style: { textAlign: 'center' } }, "Item ID"
                            th { key: 'bomheader-jsl', style: { textAlign: 'center' } }, "Jita Sell"
                            th { key: 'bomheader-jspl', style: { textAlign: 'center' } }, "Jita Split"
                        ]
                        # tbody { key: 'bombody' }, [
                        @state.billOfMaterials.map (row) ->
                            row.addCosts = ac
                            row.key = 'br-' + i
                            i += 1
                            # row.isChild = false
                            bomeFactory row, null
                        # ]
                        bomfFactory { key: @state.keyfix + '-bomf', ref: 'bomFooter'}, null
                    ]
                ]
            ]
        else
            div { key: 'bomrow' }, null

MasterView = React.createClass
    si: (item) ->
        # alert item.name + ' / ' + item.id
        this.refs.blueprintDetail.loadBillOfMaterials(item.id)

    render: ->
        isf = React.createFactory ItemSearch
        bdf = React.createFactory BlueprintDetails
        lb = React.createFactory LoginButton
        div { key: 'app' }, [
            nav { key: 'nav', className: 'navbar navbar-inverse navbar-fixed-top' }, [
                div { key: 'navheader', className: 'navbar-header' }, [
                    a { key: 'brandname', className: 'navbar-brand', href: '#' }, "Kartel"
                ]
                ul { key: 'navbody', className: 'nav navbar-nav navbar-right' }, [
                    lb { key: 'loginbutton' }, null
                    li { key: 'loginpadding', style: { paddingRight: '15px'}}, ' '
                ]
            ]
            div { key: 'appbody' }, [
                isf { key: 'app-isf', onItemSelected: this.si }, null
                bdf { key: 'app-bdf', ref: 'blueprintDetail' }, null
            ]
        ]

masterView = React.createElement MasterView, null

rendered = React.render masterView, document.getElementById('content')
require.config {
    paths: {
        react: '../lib/react/react'
    }
}

require ['react', 'item_search', 'login_button', 'bill_of_materials'], (React, ItemSearch, LoginButton, BillOfMaterials) -> 
    asset_router = ar.controllers.Assets

    {div, nav, a, ul, li} = React.DOM
    
    MasterView = React.createClass
        si: (item) ->
            this.refs.blueprintDetail.loadBillOfMaterials(item.id, item.name)
            
        render: ->
            isf = React.createFactory ItemSearch
            bdf = React.createFactory BillOfMaterials
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

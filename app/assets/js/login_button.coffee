define ['react'], (React) ->
    {ul, li, a, span} = React.DOM
    
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
    
    LoginButton
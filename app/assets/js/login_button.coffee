define ['react'], (React) ->
    asset_router = ar.controllers.Assets
    {ul, li, a, span, img} = React.DOM
    
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
            loginPath = plunderbunderConfig.eve_login
            
            li { key: 'lili' }, [
                a {"href": loginPath}, [
                    img {"src": eve_login_img_src, "key":"img"}, null
                ]
            ]
    
        setMode: (event) ->
            @props.editProfile()
            
        render: ->
            unless this.state.ajaxLoading
                unless this.state.logged_in
                    this.renderLoggedIn()
                else
                    li { key: 'dd', className: 'dropdown' }, [
                        a { key: 'a', href: '#', className: 'dropdown-toggle', role: 'button', 'data-toggle': 'dropdown'}, [
                            "Welcome, " + @state.profile.characterName
                            span { key: 'crt', className: 'caret' }, null
                        ]
                        ul { key: 'mnu', className: 'dropdown-menu', role: 'menu' }, [
                            li { key: 'prf' }, [
                                a { 
                                    key: 'edit'
                                    href: '#'
                                    onClick: @setMode
                                    id: 'editProfile'
                                }, "Edit Profile"
                            ]
                            li { key: 'lo' }, [
                                a { 
                                    key: 'a'
                                    href: jsRoutes.controllers.Authentication.logout().url 
                                }, "Log Out"
                            ]
                        ]

                    ]
            else
                li { key: 'lb-dd2' }, [
                    a { key: 'ldn', href: '#'}, "Loading..."
                ]
        componentDidMount: ->
            userApi = jsRoutes.controllers.User.user()
            $.ajax userApi.url
            .done ((result) ->
                if this.isMounted()
                    @setState { logged_in: true, profile: result,  ajaxLoading: false}
                ).bind this
            .fail ((jqXHR, textStatus, errorThrown) ->
                resultCode = jqXHR.status
                if this.isMounted()
                    @setState { logged_in: false, character: null, ajaxLoading: false }
            ).bind this
    
    LoginButton
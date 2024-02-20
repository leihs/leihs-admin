import { useState, useRef, useEffect } from 'react'
import s from './sidebar.module.scss'
import cx from 'classnames'
import { faChevronDown, faChevronRight } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

function Sidebar({ children, className }) {
  const [open, setOpen] = useState(false)
  const [hasItems, setHasItems] = useState(true)
  const ref = useRef(null)

  function handleKeyDown(event) {
    const keycode = event.keyCode
    if (keycode !== 38 && keycode !== 40) return
    event.preventDefault()

    const siblingFromKeycode = {
      38: 'previousSibling',
      40: 'nextSibling'
    }

    const parent = event.target.parentNode
    const sibling = parent[siblingFromKeycode[keycode]]

    sibling && sibling.querySelector('a, button').focus()
  }

  function handleClick() {
    return () => {
      setOpen(prev => !prev)
    }
  }

  useEffect(() => {
    if (ref.current) {
      ref.current.addEventListener('keydown', handleKeyDown)
    }

    return () => {
      if (ref.current) {
        ref.current.removeEventListener('keydown', handleKeyDown)
      }
    }
  }, [])

  useEffect(() => {
    if (!children || !ref.current) return

    const items = ref.current.querySelectorAll("[class^='item']")
    if (!items) {
      ref.current.removeEventListener('keydown', handleKeyDown)
    }

    setHasItems(items.length)
  }, [children, ref])

  if (!children || !hasItems) return null

  return (
    <nav ref={ref} role="navigation" className={cx(s['sidebar'], open && s['open'], className)}>
      <ul className={cx(s['list'])}>{children}</ul>
      <button className={cx(s['open-menu'])} type="button" onClick={handleClick()}>
        <FontAwesomeIcon icon={faChevronRight} className={cx(s['arrow'], open && s['open'])} />
      </button>
    </nav>
  )
}

function Section({ title, children, className }) {
  const listRef = useRef(null)
  const [hasItems, setHasItems] = useState(true)

  useEffect(() => {
    if (!children || !listRef.current) return

    const items = listRef.current.querySelectorAll("[class^='item']")

    setHasItems(items.length)
  }, [children, listRef, hasItems])

  // return nothing when children are empty
  if (!hasItems || !children) return null

  return (
    <li>
      <h1 role="heading" className={cx(s['section-title'], className)}>
        {title}
      </h1>
      <ul ref={listRef} role="list" className={cx(s.section, s['list'], className)}>
        {children}
      </ul>
    </li>
  )
}

function Item({ icon = null, href = null, active = false, children, className }) {
  // const [active, setActive] = useState(false) // State to manage active link
  //
  // function handleURLChange() {
  //   // Get the current URL
  //   const currentURL = window.location.href
  //   // Check if the URL contains a specific href
  //   const isHrefInURL = currentURL.includes(href)
  //   // Update isActive based on the condition
  //   setActive(isHrefInURL)
  // }
  //
  // useEffect(() => {
  //   // Add event listener for 'popstate' event (triggered by pushstate)
  //   window.addEventListener('popstate', handleURLChange)
  //   // Cleanup: remove event listener when the component unmounts
  //   return () => {
  //     window.removeEventListener('popstate', handleURLChange)
  //   }
  // }, []) // This effect runs only once on component mount

  return (
    <li role="listitem" className={cx(s['item'], className)}>
      <a draggable="false" href={href} role="link" className={cx(s['link'], active && s['active'], className)}>
        {icon && <FontAwesomeIcon icon={icon} className={cx(s['icon'])} />}
        {children}
      </a>
    </li>
  )
}

function Group({ icon = null, title = '', children, className }) {
  const [open, setOpen] = useState(false)
  const [hasItems, setHasItems] = useState(true)
  const ref = useRef(null)
  const id = crypto.randomUUID()

  function handleOpen() {
    return () => {
      setOpen(prev => !prev)
    }
  }

  function handleEsc(event) {
    if (event.keyCode !== 27) return

    const parent = ref.current.parentNode
    parent.querySelector('a, button').focus()
    setOpen(false)
  }

  function onTransitionEnd() {
    if (!ref.current) return
    const first = ref.current.firstChild
    if (!first) return
    first.querySelector('a, button').focus()
  }

  // add event listener for esc and transitionend
  useEffect(() => {
    if (!ref.current) return

    ref.current.addEventListener('transitionend', onTransitionEnd)
    ref.current.addEventListener('keydown', handleEsc)

    return () => {
      if (ref.current) {
        ref.current.removeEventListener('transitionend', onTransitionEnd)
        ref.current.removeEventListener('keydown', handleEsc)
      }
    }
  }, [])

  useEffect(() => {
    if (!children || !ref.current) return

    const items = ref.current.querySelectorAll("[class^='item']")
    if (!items) {
      ref.current.removeEventListener('transitionend', onTransitionEnd)
      ref.current.removeEventListener('keydown', handleEsc)
    }

    setHasItems(items.length)
  }, [children, ref])

  if (!children || !hasItems) return null

  return (
    <li className={cx(s['group'], s['item'], className)}>
      <button
        aria-haspopup="menu"
        aria-expanded={open ? 'true' : 'false'}
        aria-controls={id}
        type="button"
        onClick={handleOpen()}
        className={cx(s['group-button'])}
      >
        {icon && <FontAwesomeIcon icon={icon} className={cx(s['icon'])} />}
        {title}
        <FontAwesomeIcon icon={faChevronDown} className={cx(s['arrow'], open && s['open'])} />
      </button>
      <ul ref={ref} id={id} className={cx(s['group-items'], s['list'], open && s['open'], className || '')}>
        {children}
      </ul>
    </li>
  )
}

Sidebar.Item = Item
Sidebar.Section = Section
Sidebar.Group = Group

export default Sidebar

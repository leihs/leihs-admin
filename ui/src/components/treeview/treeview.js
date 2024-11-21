import React from 'react'
import { default as DefaultTreeView, flattenTree } from 'react-accessible-treeview'
import cx from 'classnames'
import s from './treeview.module.scss'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { data } from './sample'
import { faPlus, faMinus, faX } from '@fortawesome/free-solid-svg-icons'
import NoImageSVG from './no-image.svg'

console.debug(data)
const folder = data

// const folder = {
//   name: '',
//   children: [
//     {
//       name: 'src',
//       children: [
//         {
//           name: 'index.js',
//
//           children: [{ name: 'index.js' }, { name: 'styles.css' }, { name: 'styles.css' }, { name: 'test.css' }]
//         },
//         { name: 'sample.js' }
//       ]
//     },
//     {
//       name: 'node_modules',
//       metadata: { isDirectory: true },
//       children: [
//         {
//           name: 'react-accessible-treeview',
//           children: [{ name: 'bundle.js' }]
//         },
//         { name: 'react', children: [{ name: 'bundle.js' }] }
//       ]
//     },
//     {
//       name: '.npmignore'
//     },
//     {
//       name: 'package.json'
//     },
//     {
//       name: 'webpack.config.js',
//       children: [
//         {
//           name: 'react-accessible-treeview',
//           children: [{ name: 'bundle.js' }]
//         },
//         { name: 'react', children: [{ name: 'bundle.js' }] }
//       ]
//     }
//   ]
// }

function TreeElement({ element, searchTerm, getNodeProps, handleSelect, level, isExpanded, isSelectable, onSelected }) {
  function handleClick(event) {
    handleSelect(event)
    onSelected(element)
    event.stopPropagation()
  }

  function highlightText(text, highlight) {
    if (!highlight) return text
    const regex = new RegExp(`(${highlight})`, 'gi')
    const parts = text.split(regex)
    return parts.map((part, index) =>
      part.toLowerCase() === highlight.toLowerCase() ? <strong key={index}>{part}</strong> : part
    )
  }

  return (
    <div
      {...getNodeProps()}
      className={cx(isExpanded && s['expanded'], s['element'], element.children.length > 0 && s['pointer'])}
      style={{ paddingLeft: 20 * (level - 1) }}
    >
      <div className={cx(s['indent'], 'd-flex align-items-center')}>
        <div className={cx(s['controls'])} style={{ paddingLeft: 20 * (level - 1) }}>
          {element.children.length > 0 && (
            <>
              <FontAwesomeIcon className={s['icon']} icon={isExpanded ? faMinus : faPlus} />
              <span className="badge badge-secondary">{element.children.length}</span>
            </>
          )}
        </div>
        {element.metadata.thumbnail_url ? (
          <img src={element.metadata.thumbnail_url} width="32" height="32" />
        ) : (
          <NoImageSVG className={s['thumbnail']} />
        )}

        {onSelected ? (
          <span className="ml-3">{highlightText(element.name, searchTerm)} </span>
        ) : (
          <a
            className="ml-3"
            role="link"
            // onClick={ev => ev.stopPropagation()}
            href={`/admin/categories/${element.metadata.id}`}
          >
            {highlightText(element.name, searchTerm)}{' '}
          </a>
        )}
        {onSelected ? (
          <button
            onClick={event => handleClick(event)}
            type="button"
            className={cx(s['visible-on-hover'], 'btn btn-primary ml-auto mr-4')}
          >
            Select
          </button>
        ) : (
          <span className={cx('ml-auto mr-4', element.metadata.models_count === 0 && 'text-danger')}>
            {element.metadata.models_count} Modelle
          </span>
        )}
      </div>
    </div>
  )
}

function TreeView({ data = folder, onSelected = null }) {
  const flattenedData = flattenTree(data)
  const allNodeIds = flattenedData.map(node => node.id)

  const [searchTerm, setSearchTerm] = React.useState('')
  const [expandedNodeIds, setExpandedNodeIds] = React.useState([])
  const [filteredNodes, setFilteredNodes] = React.useState(flattenedData)
  const [matchingExpanded, setMatchingExpanded] = React.useState([])

  React.useEffect(() => {
    console.debug('matching changed')
  }, [matchingExpanded])

  function reset() {
    setSearchTerm('')
    setFilteredNodes(flattenedData)
    setExpandedNodeIds([])
  }

  function filterNodes() {
    if (searchTerm === '') {
      reset()
      return
    }

    const matchingNodes = flattenedData.filter(node => node.name.toLowerCase().includes(searchTerm.toLowerCase()))

    const findAncestors = (node, data, result) => {
      if (node.parent === 0) {
        return
      }

      const parentNode = data.find(item => item.id === node.parent)
      if (parentNode) {
        result.push(parentNode)
        findAncestors(parentNode, data, result)
      }
    }

    const getAllMatchingNodesWithAncestors = (matchingNodes, flattenedData) => {
      const result = []

      matchingNodes.forEach(node => {
        result.push(node)
        findAncestors(node, flattenedData, result)
      })

      // Remove duplicates
      const uniqueResult = []
      const seenIds = new Set()

      result.forEach(item => {
        if (!seenIds.has(item.id)) {
          uniqueResult.push(item)
          seenIds.add(item.id)
        }
      })

      return uniqueResult
    }

    const matchingNodesWithAncestors = getAllMatchingNodesWithAncestors(matchingNodes, flattenedData)

    const filteredTopLevelNodes = matchingNodesWithAncestors.reduce((acc, element) => {
      if (element.parent === 0) {
        acc.push(element)
      }

      return acc
    }, [])

    const [rootNode, ...remainingNodes] = flattenedData
    rootNode.children = filteredTopLevelNodes.map(node => node.id)
    const updatedFilteredNodes = [rootNode, ...remainingNodes]

    const expandedIds = matchingNodes.map(node => node.parent)

    const arraysAreEqual = (array1, array2) => {
      if (!array1 || !array2) return false

      if (array1.length !== array2.length) {
        return false
      }

      for (let i = 0; i < array1.length; i++) {
        if (array1[i] !== array2[i]) {
          return false
        }
      }

      return true
    }

    setFilteredNodes(updatedFilteredNodes)
    setMatchingExpanded(expandedIds)
    // setExpandedNodeIds(expandedIds)

    // if (arraysAreEqual(expandedIds, expandedNodeIds)) {
    //   setExpandedNodeIds([])
    // } else {
    //   setExpandedNodeIds(expandedIds)
    // }
  }

  return (
    <div className={cx(s['card-no-border'], 'card')}>
      <div className={cx(s['card-header-border'], 'card-header')}>
        <div className="btn-toolbar" role="toolbar" aria-label="Toolbar with button groups">
          <div className="input-group mr-4">
            <input
              type="text"
              value={searchTerm}
              onChange={event => setSearchTerm(event.target.value)}
              onKeyUp={_ => filterNodes()}
              autoComplete="off"
              className="form-control"
              placeholder="Search"
              aria-label="Category search field"
            />
            <div className="input-group-append">
              <button className="btn btn-secondary" type="button" id="button-addon2">
                <FontAwesomeIcon className="mx-1" icon={faX} onClick={() => reset()} />
              </button>
            </div>
          </div>
          <button
            type="button"
            className="btn btn-outline-secondary"
            onClick={() => setExpandedNodeIds(matchingExpanded)}
          >
            <FontAwesomeIcon className="mr-2" icon={faPlus} />
            open searched ones
          </button>
          <button type="button" className="btn btn-outline-secondary" onClick={() => setExpandedNodeIds(allNodeIds)}>
            <FontAwesomeIcon className="mr-2" icon={faPlus} />
            open all
          </button>
          <button type="button" className="btn btn-outline-secondary ml-4" onClick={() => setExpandedNodeIds([])}>
            <FontAwesomeIcon className="mr-2" icon={faMinus} />
            close all
          </button>
        </div>
      </div>

      <DefaultTreeView
        data={filteredNodes}
        className={s['list']}
        expandedIds={expandedNodeIds}
        nodeRenderer={({ element, getNodeProps, level, isExpanded, handleSelect }) => (
          <TreeElement
            searchTerm={searchTerm}
            element={element}
            getNodeProps={getNodeProps}
            level={level}
            isExpanded={isExpanded}
            handleSelect={handleSelect}
            onSelected={onSelected}
          />
        )}
      />
    </div>
  )
}

export default TreeView
